package com.dobbinsoft.fw.core.entiy;

import com.dobbinsoft.fw.core.annotation.param.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by rize on 2019/7/1.
 */
@Getter
@Setter
public class SuperVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private Long id;

    private LocalDateTime gmtUpdate;

    private LocalDateTime gmtCreate;

}
