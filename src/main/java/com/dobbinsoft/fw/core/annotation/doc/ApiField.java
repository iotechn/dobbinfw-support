package com.dobbinsoft.fw.core.annotation.doc;

import com.dobbinsoft.fw.core.enums.BaseEnums;
import com.dobbinsoft.fw.core.enums.EmptyEnums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * Description: 描述实体属性
 * User: rize
 * Date: 2021-03-17
 * Time: 上午9:15
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiField {

    String description();

    Class<? extends BaseEnums> enums() default EmptyEnums.class;

    boolean hidden() default false;

}
