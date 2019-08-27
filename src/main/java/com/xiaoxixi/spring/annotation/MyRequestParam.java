package com.xiaoxixi.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER})
public @interface MyRequestParam {
    String name() default "";
}
