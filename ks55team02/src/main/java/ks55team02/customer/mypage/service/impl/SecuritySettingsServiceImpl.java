package ks55team02.customer.mypage.service.impl;

import ks55team02.customer.mypage.domain.PasswordChangeRequest;
import ks55team02.customer.mypage.mapper.SecuritySettingsMapper;
import ks55team02.customer.mypage.service.SecuritySettingsService;
import ks55team02.systems.crypto.utils.PasswordEncryptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SecuritySettingsServiceImpl implements SecuritySettingsService{
	
	private final SecuritySettingsMapper securitySettingsMapper;

	@Override
	public boolean changePassword(String userNo, PasswordChangeRequest request) {
        log.info("비밀번호 변경 시도 - 사용자: {}", userNo);

        // 1. 새 비밀번호와 확인 비밀번호 일치 여부 검증
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            log.warn("새 비밀번호와 확인 비밀번호 불일치 - 사용자: {}", userNo);
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 2. 현재 비밀번호 검증
        String storedEncryptedPassword = securitySettingsMapper.selectUserEncryptedPasswordByNo(userNo);
        if (storedEncryptedPassword == null) {
            log.error("사용자 정보 없음 또는 비밀번호 없음 - userNo: {}", userNo);
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."); // 또는 UserNotFoundException
        }

        // PasswordEncryptor의 checkPassword 메서드를 사용하여 현재 비밀번호 일치 여부 확인
        if (!PasswordEncryptor.checkPassword(request.getCurrentPassword(), storedEncryptedPassword)) {
            log.warn("현재 비밀번호 불일치 - 사용자: {}", userNo);
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            log.warn("현재 비밀번호와 새 비밀번호가 동일합니다 - 사용자: {}", userNo);
            throw new IllegalArgumentException("현재 비밀번호와 다른 새 비밀번호를 입력해주세요.");
        }

        // 3. 새 비밀번호 암호화
        String newEncryptedPassword = PasswordEncryptor.hashPassword(request.getNewPassword());
        log.debug("새 비밀번호 암호화 완료");

        // 4. 비밀번호 및 변경일 업데이트 (트랜잭션 관리)
        int updatedUsersCount = securitySettingsMapper.updateUserPassword(userNo, newEncryptedPassword);
        if (updatedUsersCount == 0) {
            log.error("users 테이블 비밀번호 업데이트 실패 - 사용자: {}", userNo);
            throw new RuntimeException("비밀번호 업데이트에 실패했습니다. (users 테이블)");
        }
        log.debug("users 테이블 비밀번호 업데이트 성공 - 행 수: {}", updatedUsersCount);

        LocalDateTime now = LocalDateTime.now();
        int updatedSecuritySettingsCount = securitySettingsMapper.updatePasswordChangeDate(userNo, now);
        // user_security_settings 테이블은 첫 비밀번호 변경 시 INSERT 될 수 있으므로, 0이 아니면 성공으로 간주
        if (updatedSecuritySettingsCount == 0) {
            log.warn("user_security_settings 테이블 비밀번호 변경일 업데이트/삽입 실패 (0행 영향) - 사용자: {}", userNo);
            throw new RuntimeException("비밀번호 변경일 업데이트에 실패했습니다. (user_security_settings 테이블)");
        }
        log.debug("user_security_settings 테이블 비밀번호 변경일 업데이트/삽입 성공 - 행 수: {}", updatedSecuritySettingsCount);

        log.info("비밀번호 변경 성공 - 사용자: {}", userNo);
        return true;
    }
}
