package ks55team02.systems.crypto.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptor {
	
	private static final String ALGORITHM = "SHA-256";
    private static final int SALT_SIZE = 16;

    /**
     * 비밀번호를 해싱(단방향 암호화)하는 메소드
     * @param password 평문 비밀번호
     * @return salt와 해싱된 비밀번호를 합쳐 Base64로 인코딩한 문자열
     */
    public static String hashPassword(String password) {
        try {
            // 1. 솔트(Salt) 생성
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_SIZE];
            random.nextBytes(salt);

            // 2. 비밀번호와 솔트를 합쳐 해싱
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 3. 솔트와 해싱된 비밀번호를 합쳐서 저장 (salt + hashedPassword)
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);

            // 4. Base64로 인코딩하여 반환
            return Base64.getEncoder().encodeToString(combined);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }

    /**
     * 입력된 비밀번호와 저장된 해시 값을 비교하는 메소드
     * @param password 사용자가 입력한 평문 비밀번호
     * @param storedHash DB에 저장된 해시 값
     * @return 일치하면 true, 아니면 false
     */
    public static boolean checkPassword(String password, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            // 1. 저장된 해시 값에서 솔트(Salt) 분리
            byte[] salt = new byte[SALT_SIZE];
            System.arraycopy(combined, 0, salt, 0, SALT_SIZE);

            // 2. 입력된 비밀번호를 동일한 솔트로 해싱
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());

            // 3. DB에서 가져온 해시 부분과 방금 생성한 해시를 비교
            byte[] storedPasswordPart = new byte[combined.length - SALT_SIZE];
            System.arraycopy(combined, SALT_SIZE, storedPasswordPart, 0, storedPasswordPart.length);

            return MessageDigest.isEqual(hashedPassword, storedPasswordPart);
        } catch (Exception e) {
            // 디코딩 실패 등 예외 발생 시 안전하게 false 반환
            return false;
        }
    }
}
