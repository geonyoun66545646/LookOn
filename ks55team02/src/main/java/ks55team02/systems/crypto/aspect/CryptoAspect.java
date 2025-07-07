package ks55team02.systems.crypto.aspect;

import java.util.Set;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import ks55team02.systems.crypto.annotation.Decrypt;
import ks55team02.systems.crypto.annotation.Encrypt;
import ks55team02.systems.crypto.utils.AesEncryptor;
import ks55team02.systems.crypto.utils.CryptUtils;
import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class CryptoAspect {

	private final AesEncryptor encryptor;

    // insert 전에 암호화 수행
    @Before("@annotation(encrypt)")
    public void beforeEncrypt(JoinPoint joinPoint, Encrypt encrypt) {
    	Set<String> fields = Set.of(encrypt.only());
        for (Object arg : joinPoint.getArgs()) {
            CryptUtils.encryptFields(arg, encryptor, fields);
        }
    }

    // select 후 복호화 수행
    @AfterReturning(value = "@annotation(decrypt)", returning = "result")
    public void afterDecrypt(JoinPoint joinPoint, Decrypt decrypt, Object result) {
        Set<String> fields = Set.of(decrypt.only());
        CryptUtils.decryptFields(result, encryptor, fields);
    }
}
