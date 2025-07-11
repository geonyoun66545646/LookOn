package ks55team02.systems.crypto.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchHashUtils {

	private static final Logger log = LoggerFactory.getLogger(SearchHashUtils.class);

    // 검색용으로 사용할 고정 Salt 값. 외부에 노출되지 않도록 주의.
    private static final String SEARCH_SALT = "ks55-team02-search-salt-value*&^%";
    private static final String ALGORITHM = "SHA-256";

    /**
     * 입력된 평문을 검색용 해시 값으로 변환합니다.
     * @param plainText 해시할 평문 데이터 (이메일, 전화번호 등)
     * @return SHA-256으로 해시된 64자리 Hex 문자열
     */
    public static String createHash(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            // 고정된 Salt를 먼저 적용
            md.update(SEARCH_SALT.getBytes());
            // 그 다음 평문을 적용
            byte[] hashedBytes = md.digest(plainText.getBytes());

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("해시 생성 중 에러 발생 (알고리즘: {})", ALGORITHM, e);
            throw new RuntimeException("해시 생성에 실패했습니다.", e);
        }
    }
}
