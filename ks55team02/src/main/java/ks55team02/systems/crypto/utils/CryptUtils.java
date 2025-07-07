package ks55team02.systems.crypto.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public class CryptUtils {

	// 암호화 (선택적 필드 지원)
    public static void encryptFields(Object dto, AesEncryptor encryptor, Set<String> onlyFields) {
        if (dto instanceof Collection<?> col) {
            col.forEach(obj -> encryptFields(obj, encryptor, onlyFields));
            return;
        }

        for (Field field : dto.getClass().getDeclaredFields()) {
        	if (!onlyFields.isEmpty() && !onlyFields.contains(field.getName())) continue;
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

    // 복호화 (선택적 필드 지원)
    public static void decryptFields(Object dto, AesEncryptor encryptor, Set<String> onlyFields) {
        if (dto instanceof Collection<?> col) {
            col.forEach(obj -> decryptFields(obj, encryptor, onlyFields));
            return;
        }

        for (Field field : dto.getClass().getDeclaredFields()) {
            
            if (!onlyFields.isEmpty() && !onlyFields.contains(field.getName())) continue;

            try {
                field.setAccessible(true);
                Object value = field.get(dto);
                if (value instanceof String ciphertext && ciphertext != null) {
                	// 기존 데이터 {noop} == no operation 포함시 복호화 연산안함, 이후 암호화 되어 있는 값은 복호화 연산
                	ciphertext = ciphertext.contains("{noop}") ? ciphertext.replace("{noop}", "").trim() : encryptor.decrypt(ciphertext);
                	// 최종 결과 기존 객체에 할당
                    field.set(dto, ciphertext);
                }
            } catch (Exception e) {
                throw new RuntimeException("복호화 실패", e);
            }
        }
    }
}
