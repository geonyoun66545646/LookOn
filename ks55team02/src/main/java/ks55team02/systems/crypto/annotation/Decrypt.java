package ks55team02.systems.crypto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Decrypt {
	/**
	 * 암호화 대상 필드 이름 지정
	 * @Decrypt(only={"filed1","filed2", ...}) : 지정된 필드
	 * @Decrypt : 전체 필드
	 */
	String[] only() default {};
}
