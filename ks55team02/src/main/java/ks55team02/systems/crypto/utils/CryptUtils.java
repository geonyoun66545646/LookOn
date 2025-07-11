package ks55team02.systems.crypto.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ks55team02.systems.crypto.annotation.EncryptedField;

public class CryptUtils {

	private static final Logger log = LoggerFactory.getLogger(CryptUtils.class);
	
	// 암호화 (선택적 필드 지원)
    public static void encryptFields(Object dto, AesEncryptor encryptor) {
    	if (dto == null) return;
        
        if (dto instanceof Collection<?> col) {
            col.forEach(obj -> encryptFields(obj, encryptor)); // 파라미터 제거
            return;
        }

        for (Field field : dto.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(EncryptedField.class)) {
            	try {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    if (value instanceof String plaintext && plaintext != null) {
                        field.set(dto, encryptor.encrypt(plaintext));
                    }
                } catch (Exception e) {
                    throw new RuntimeException("암호화 실패", e);
                }
            }
        }
    }

    
    // 복호화 (선택적 필드 지원)
    public static void decryptFields(Object dto, AesEncryptor encryptor) {
    	if (dto == null) return;
    	
        if (dto instanceof Collection<?> col) {
            col.forEach(obj -> decryptFields(obj, encryptor));
            return;
        }

        for (Field field : dto.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(EncryptedField.class)) {
                log.info("  -> @EncryptedField 발견: {}", field.getName());
                try {
                    field.setAccessible(true);
                    Object value = field.get(dto);
                    if (value instanceof String ciphertext && ciphertext != null && !ciphertext.isEmpty()) {
                        log.info("    >> 복호화 시도: [{}]", ciphertext);
                        
                        String decryptedValue;
                        // {noop} 처리 로직
                        if (ciphertext.contains("{noop}")) {
                            decryptedValue = ciphertext.replace("{noop}", "").trim();
                            log.info("    >> 결과 ({noop} 처리): [{}]", decryptedValue);
                        } else {
                            try {
                                // 올바른 복호화 호출
                                decryptedValue = encryptor.decrypt(ciphertext.trim());
                                log.info("    >> 결과 (복호화 성공): [{}]", decryptedValue);
                            } catch (Exception e) {
                                log.error("    >> 복호화 실패! 원인: {}", e.getMessage());
                                decryptedValue = ciphertext; // 실패 시 원본 유지
                            }
                        }
                        field.set(dto, decryptedValue);
                    } else {
                        log.warn("  -> @EncryptedField 필드 '{}'의 값이 문자열이 아니거나 비어있어 건너뜁니다. (타입: {})", 
                                 field.getName(), (value == null ? "null" : value.getClass().getSimpleName()));
                    }
                } catch (Exception e) {
                    log.error("  -> 필드 '{}' 처리 중 심각한 예외 발생!", field.getName(), e);
                }
            }
        }
    }
}
