package com.dobbinsoft.fw.support.component.open;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.alibaba.fastjson.JSONObject;
import com.dobbinsoft.fw.support.component.open.model.OPData;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.springframework.util.ObjectUtils;

public class OpenPlatformUtil {

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

}
