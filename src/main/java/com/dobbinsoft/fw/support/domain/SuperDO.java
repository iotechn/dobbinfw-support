package com.dobbinsoft.fw.support.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.Date;

/**
 * Created by rize on 2019/6/30.
 */
@Data
public class SuperDO {

    private Long id;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtUpdate;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

}
