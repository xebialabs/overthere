package com.xebialabs.overthere;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation can be used to signify that a class is an Overthere Protocol.
 * It will be registered on startup of the application and will register under the provided name.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Protocol {
	String name();
}
