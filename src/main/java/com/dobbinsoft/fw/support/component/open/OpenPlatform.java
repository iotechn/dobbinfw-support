package com.dobbinsoft.fw.support.component.open;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.component.open.exception.OpenPlatformException;
import com.dobbinsoft.fw.support.component.open.model.*;
import com.squareup.okhttp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ClassName: AbstractOpenPlatform
 * Description: 一个抽象的开放平台，定义了抽象的功能，持久层方案，由业务层决定
 *
 * @author: e-weichaozheng
 * @date: 2021-04-23
 */
@Slf4j
public class OpenPlatform {

    @Autowired
    protected OpenPlatformStorageStrategy openPlatformStorageStrategy;

    private OkHttpClient okHttpClient = new OkHttpClient();

    private ThreadLocal<String> clientCodeThreadLocal = new ThreadLocal<>();


    public void setClientCode(String clientCode) {
        clientCodeThreadLocal.set(clientCode);
    }

    public String getClientCode() {
        return clientCodeThreadLocal.get();
    }

    public void removeClientCode() {
        clientCodeThreadLocal.remove();
    }

    /**
     * 初始化客户端
     *
     * @param client
     * @return
     */
    public OPClient init(OPClient client) throws OpenPlatformException {
        // 1. 校验Code是否重复
        if (StrUtil.isEmpty(client.getCode())) {
            throw new OpenPlatformException("客户代号不能为空");
        }
        if (openPlatformStorageStrategy.checkClientExists(client.getCode())) {
            throw new OpenPlatformException("客户代号重复，请换一个");
        }
        // 2. 持久化
        OPClient opClient = openPlatformStorageStrategy.initClient(client.getCode(), client.getNotifyUrl(), client.getPublicKey2());
        opClient.setPrivateKey1(null);
        return opClient;
    }

    public Boolean setPublicKey(String clientCode, String publicKey) {
        if (StrUtil.isEmpty(clientCode)) {
            throw new OpenPlatformException("客户代号不能为空");
        }
        if (StrUtil.isEmpty(publicKey)) {
            throw new OpenPlatformException("公钥不能为空");
        }
        return openPlatformStorageStrategy.setClientPublicKey(clientCode, publicKey);
    }

    public Boolean setNotifyUrl(String clientCode, String notifyUrl) {
        if (StrUtil.isEmpty(clientCode)) {
            throw new OpenPlatformException("客户代号不能为空");
        }
        if (StrUtil.isEmpty(notifyUrl)) {
            throw new OpenPlatformException("回调地址不能为空");
        }
        return openPlatformStorageStrategy.setClientNotifyUrl(clientCode, notifyUrl);
    }

    public OPClient getClient(String clientCode) {
        OPClient client = openPlatformStorageStrategy.getClient(clientCode);
        if (client == null) {
            throw new OpenPlatformException("客户代号并不存在");
        }
        return client;
    }

    /**
     * 获取客户权限列表
     *
     * @param clientCode
     * @return
     */
    public Boolean setPermissionList(String clientCode, List<OPClientPermission> permissions) {
        if (StrUtil.isEmpty(clientCode)) {
            throw new OpenPlatformException("客户代号不能为空");
        }
        return openPlatformStorageStrategy.setClientPermissionList(clientCode, permissions);
    }

    public Boolean checkApiPermission(String clientCode, String group, String method) {
        if (StrUtil.isEmpty(clientCode)) {
            throw new OpenPlatformException("客户代号不能为空");
        }
        return openPlatformStorageStrategy.checkApiPermission(clientCode, group, method);
    }

    public Map<String, String[]> decrypt(String clientCode, String ciphertext) {
        String publicKey2 = openPlatformStorageStrategy.getClient(clientCode).getPublicKey2();
        String parameterMapStr = new String(SecureUtil.rsa(null, publicKey2).decrypt(ciphertext, KeyType.PublicKey), StandardCharsets.UTF_8);
        String[] pairs = parameterMapStr.split("&");
        Map<String, String[]> map = new HashMap<>();
        for (String v : pairs) {
            if (!StringUtils.isEmpty(v)) {
                String[] pair = v.split("=");
                if (pair.length > 1 && !StringUtils.isEmpty(pair[0])) {
                    map.put(pair[0], new String[] {pair[1]});
                }
            }
        }
        return map;
    }


    /**
     * 向客户回调地址发送请求
     *
     * @param clientCode
     * @param params     请求参数
     */
    public void sendNotify(String clientCode, Map<String, String> params) {
        params.put("opversion", UUID.randomUUID().toString());
        List<String> paramsRawList = params.keySet().stream().map(item -> (item + "=" + params.get(item))).collect(Collectors.toList());
        OPNotify opNotify = new OPNotify();
        opNotify.setParams(paramsRawList);
        opNotify.setClientCode(clientCode);
        int status = this.sendNotify(opNotify);
        opNotify.setStatus(status);
        if (status == 0) {
            opNotify.setNextNotify(new Date(System.currentTimeMillis() + 3000l));
        } else {
            opNotify.setNextNotify(new Date());
        }
        openPlatformStorageStrategy.storeNotify(opNotify);

    }

    public int sendNotify(OPNotify opNotify) {
        try {
            opNotify.getParams().add("timestamp=" + System.currentTimeMillis());
            opNotify.getParams().sort(String::compareTo);
            // 发送一次Http请求
            String rawBody = opNotify.getParams().stream().collect(Collectors.joining("&"));
            OPData opData = new OPData();
            opData.setClientCode(opNotify.getClientCode());
            OPClient clientWithPK = openPlatformStorageStrategy.getClientWithPK(opNotify.getClientCode());
            opData.setCiphertext(SecureUtil.rsa(clientWithPK.getPrivateKey1(),
                    clientWithPK.getPublicKey1()).encryptBase64(rawBody, KeyType.PrivateKey));
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, JSONObject.toJSONString(opData));
            Response responseBody = okHttpClient.newCall(new Request
                    .Builder()
                    .url(clientWithPK.getNotifyUrl())
                    .post(body).build())
                    .execute();
            if (responseBody.code() == 200) {
                return OPNotifyStatusType.OK.getCode();
            } else {
                return OPNotifyStatusType.FAIL.getCode();
            }
        } catch (IOException e) {
            return OPNotifyStatusType.FAIL.getCode();
        } catch (Exception e) {
            log.error("[发送通知] 异常", e);
            return OPNotifyStatusType.FAIL.getCode();
        }
    }

}
