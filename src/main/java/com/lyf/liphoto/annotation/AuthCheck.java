package com.lyf.liphoto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName:AuthCheck
 * Package: com.lyf.liphoto.annotation
 * Description:
 *
 * @Author 黎云锋
 * @Create 2025/2/18 17:34
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     *必须具有某个角色
      */
    String mustRole() default "";
}
