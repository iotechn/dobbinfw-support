package com.dobbinsoft.fw.support.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by rize on 2019/6/30.
 */
@Getter
@Setter
public class SuperDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtUpdate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

}
