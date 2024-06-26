package com.dobbinsoft.fw.support.utils.excel;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelSheet {

    /**
     * Sheet 标题
     *
     * @return
     */
    String title() default "";

    /**
     * Sheet下标
     *
     * @return
     */
    int index() default 0;

    /**
     * 默认单元格高度，注意，单元格高度和宽度单位是不一致的
     * @return
     */
    short rowHeight() default 15;
}