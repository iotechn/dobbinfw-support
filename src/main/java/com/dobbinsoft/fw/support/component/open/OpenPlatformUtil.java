package com.dobbinsoft.fw.support.component.open;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.component.open.model.OPData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class OpenPlatformUtil {

    /**
     * 构建向开放平台发送的请求
     * @param data 请求的数据 param1=xxxx&param2=yyyy
     * @param clientCode
     * @param privateKey
     * @return
     */
    public static RequestBody buildBody(String data, String clientCode, String privateKey) {
        if (ObjectUtils.isEmpty(clientCode) || ObjectUtils.isEmpty(privateKey)) {
            throw new NullPointerException("clientCode 与 privateKey 不能为空");
        }
        if (ObjectUtils.isEmpty(data)) {
            data = "optimestamp=";
        } else {
            data += "&optimestamp=";
        }
        data += System.currentTimeMillis();
        MediaType mediaType = MediaType.parse("application/json");
        String ciphertext = SecureUtil.rsa(privateKey,
                null).encryptBase64(data, KeyType.PrivateKey);
        OPData opData = new OPData();
        opData.setClientCode(clientCode);
        opData.setCiphertext(ciphertext);
        String json = JSONObject.toJSONString(opData);
        return RequestBody.create(mediaType, json);
    }

    /**
     * 开放平台回调验签
     * @param data
     * @param publicKey1
     * @return
     */
    public static Map<String, String> parseAndCheckSign(String data, String publicKey1) {
        // 若能够用服务器公钥解析，则表明是服务器私有加密的
        String plaintext = SecureUtil.rsa(null,
                publicKey1).decryptStr(data, KeyType.PublicKey);
        String[] pairs = plaintext.split("&");
        Map<String, String> res = new HashMap<>();
        for (String pair : pairs) {
            String[] kvs = pair.split("=");
            res.put(kvs[0], kvs[1]);
        }
        return res;
    }

}
