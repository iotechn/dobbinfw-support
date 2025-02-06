package com.dobbinsoft.fw.support.lambda;

import com.dobbinsoft.fw.support.utils.JacksonUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Getter
@Setter
@Slf4j
public class LambdaDTO {

    private static final WebClient client = WebClient.create();

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
    public Mono<String> call(Map<String, Object> values) {
        WebClient.RequestBodySpec requestSpec = client.post()
                .uri(this.url)
                .header("Content-Type", "application/json");

        if (!this.headers.isEmpty()) {
            this.headers.forEach(requestSpec::header);
        }

        return requestSpec
                .bodyValue(JacksonUtil.toJSONString(values))
                .retrieve()
                .bodyToMono(String.class);


    }

}
