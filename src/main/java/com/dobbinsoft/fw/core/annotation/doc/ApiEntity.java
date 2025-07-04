package com.dobbinsoft.fw.core.annotation.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created with IntelliJ IDEA.
 * Description: 描述实体
 * User: rize
 * Date: 2021-03-17
 * Time: 上午9:15
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEntity {

    /**
     * 实体名，默认为类名 （不支持带范型的类）
     * @return
     */
    String name() default "";

    /**
     * 描述
     * @return
     */
    String description();

}
