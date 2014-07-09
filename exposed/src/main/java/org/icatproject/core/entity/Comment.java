package org.icatproject.core.entity;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({TYPE, FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Comment {

	String value();

}
