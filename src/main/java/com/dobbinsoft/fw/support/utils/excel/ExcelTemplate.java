package com.dobbinsoft.fw.support.utils.excel;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelTemplate {

    /**
     * 单元格内的值
     *
     * @return
     */
    String value() default "";

    /**
     * 合并行的数量
     *
     * @return
     */
    int rowspan() default 0;

    /**
     * 起始列
     *
     * @return
     */
    int colIndex();

    /**
     * 列合并的数量
     *
     * @return
     */
    int colspan() default 0;
}