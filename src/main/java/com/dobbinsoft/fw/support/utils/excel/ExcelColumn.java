package com.dobbinsoft.fw.support.utils.excel;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {

    // 标题
    String title();

    // 子标题，可用于格式提示
    String subTitle() default "";

    // 下标
    int index() default 0;

    // 标题行坐标
    int rowIndex() default 0;

    // 宽度
    int width() default 15;

    // 日期格式 yyyy-MM-dd
    String format() default "";

    // 可用哪些枚举
    String[] enums() default {};
}