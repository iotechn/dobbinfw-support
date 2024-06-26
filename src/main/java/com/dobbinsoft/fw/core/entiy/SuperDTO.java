package com.dobbinsoft.fw.core.entiy;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by rize on 2019/7/1.
 */
@Data
public class SuperDTO {

    private Long id;

    private LocalDateTime gmtUpdate;

    private LocalDateTime gmtCreate;

}
