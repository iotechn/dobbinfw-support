package com.dobbinsoft.fw.core.annotation.param.transfer;

import java.lang.annotation.*;

// TODO 标记字段为价格分，脚手架会自动将返回值转化为小数，将输入值自动转化为整数
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Documented
public @interface PriceCent {

}
