package com.cr.kickstart.framework.annotations;

import java.lang.annotation.*;

/**
 * Author: chrismicali
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface APIEndpointBinding {

    String value();

}
