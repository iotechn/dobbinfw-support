package com.dobbinsoft.fw.support.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Created by rize on 2019/6/30.
 */
@Data
public class SuperDO {

    private Long id;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime gmtUpdate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime gmtCreate;

}
