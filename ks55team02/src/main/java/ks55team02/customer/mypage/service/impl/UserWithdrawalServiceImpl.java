package ks55team02.customer.mypage.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.login.domain.LoginUser;
import ks55team02.customer.mypage.mapper.UserWithdrawalMapper;
import ks55team02.customer.mypage.service.UserWithdrawalService;
import ks55team02.systems.crypto.utils.PasswordEncryptor;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserWithdrawalServiceImpl implements UserWithdrawalService {

	private final UserWithdrawalMapper userWithdrawalMapper;

    @Override
    public void withdrawUser(LoginUser loginUser, String rawPassword) {
        
    	String encryptedPassword = userWithdrawalMapper.getEncryptedPasswordByUserNo(loginUser.getUserNo());
        
        // [수정] passwordEncryptor의 비밀번호 비교 메소드를 호출합니다.
        // (메소드 이름이 matches가 아닐 수 있으니 확인이 필요합니다)
    	if (encryptedPassword == null || !PasswordEncryptor.checkPassword(rawPassword, encryptedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        
        userWithdrawalMapper.processUserWithdrawal(loginUser.getUserNo());
    }
}
