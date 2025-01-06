package com.dobbinsoft.fw.support.lambda;

import com.dobbinsoft.fw.support.utils.CollectionUtils;
import com.dobbinsoft.fw.support.utils.JacksonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class LambdaDTO {

    private static final OkHttpClient client = new OkHttpClient();

    private String url;

    private String name;

    private String description;

    private Map<String, LambdaParamDTO> params;

    private Map<String, String> headers;

    // 执行完后自动添加上去
    private String callResult;



    @Getter
    @Setter
    public static class LambdaParamDTO {
        private String name;

        private Boolean required;

        private LambdaParamType type;

        private String description;
    }

    /**
     * 拼接好并调用
     * @param values
     * @return
     */
    public String call(Map<String, Object> values) {
        Request.Builder builder = new Request.Builder()
                .url(this.url)
                .post(RequestBody.create(JacksonUtil.toJSONString(values), MediaType.parse("application/json; charset=utf-8")));
        if (CollectionUtils.isNotEmpty(this.headers)) {
            headers.forEach(builder::addHeader);
        }
        try {
            String callResult = client.newCall(builder.build())
                    .execute()
                    .body()
                    .string();
            this.callResult = callResult;
            return callResult;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
