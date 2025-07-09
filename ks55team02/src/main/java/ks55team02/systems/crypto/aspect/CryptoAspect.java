package ks55team02.systems.crypto.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;


import ks55team02.systems.crypto.utils.AesEncryptor;
import ks55team02.systems.crypto.utils.CryptUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class CryptoAspect {

	private final AesEncryptor encryptor;

	/**
     * @Encrypt 어노테이션이 붙은 메소드 실행 전에 파라미터의 DTO를 암호화합니다.
     */
	@Before("@annotation(ks55team02.systems.crypto.annotation.Encrypt)")
    public void beforeEncrypt(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            CryptUtils.encryptFields(arg, encryptor);
        }
    }

	/**
     * @Decrypt 어노테이션이 붙은 메소드 실행 후에 반환된 DTO를 복호화합니다.
     */
	@AfterReturning(pointcut = "@annotation(ks55team02.systems.crypto.annotation.Decrypt)", returning = "result")
    public void afterDecrypt(JoinPoint joinPoint, Object result) {
		log.info("===> @Decrypt 메소드 감지! 타겟: {}", joinPoint.getSignature().toShortString());

	    if (result == null) { // result가 null일 경우를 대비한 방어 코드
	        log.warn("타겟 메소드가 null을 반환하여 복호화를 건너뜁니다.");
	        return;
	    }
		
        CryptUtils.decryptFields(result, encryptor);
    }
}
