package com.dobbinsoft.fw.support.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * 叶子子表， 表示子表下再无其他子表。且只关联一个外键。
 * 用法：将其注释在Dto的属性上，当使用 selectDto、deleteDto时，不指定属性，可默认当作指定
 * User: rize
 * Date: 2021/3/31
 * Time: 09:00
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LeafTable {
}
