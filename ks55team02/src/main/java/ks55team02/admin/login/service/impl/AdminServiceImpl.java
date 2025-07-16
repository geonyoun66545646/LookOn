package ks55team02.admin.login.service.impl;

import org.springframework.stereotype.Service;

import ks55team02.admin.login.domain.AdminInfo;
import ks55team02.admin.login.domain.AdminLoginRequest;
import ks55team02.admin.login.mapper.AdminMapper;
import ks55team02.admin.login.service.AdminService;
import ks55team02.customer.login.domain.LoginUser;
import ks55team02.systems.crypto.utils.PasswordEncryptor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

	// 새로 만든 AdminMapper와, 기존 암호화 유틸을 주입받습니다.
    private final AdminMapper adminMapper;
    private final PasswordEncryptor passwordEncryptor;

    @Override
    public LoginUser login(AdminLoginRequest loginRequest) {
        
        // 1. 아이디로 DB에서 관리자 정보를 가져옵니다.
        AdminInfo adminInfo = adminMapper.getAdminInfoById(loginRequest.getAdminId());

        // 2. 아이디가 존재하고, 비밀번호가 일치하는지 확인합니다.
        if (adminInfo != null && passwordEncryptor.checkPassword(loginRequest.getAdminPw(), adminInfo.getUserPswd())) {
            
            String userGrade = adminInfo.getMbrGrdCd();
            // 3. 관리자 등급인지 확인합니다.
            if ("grd_cd_0".equals(userGrade) || "grd_cd_1".equals(userGrade)) {
                
                // 4. 로그인 성공 시, 세션에 저장할 LoginUser 객체를 만들어 반환합니다.
                return new LoginUser(
                        adminInfo.getUserNo(),
                        adminInfo.getMbrGrdCd(),
                        adminInfo.getUserLgnId(),
                        adminInfo.getUserNm()
                );
            }
        }

        // 로그인에 실패하면 null을 반환합니다.
        return null;
    }
}
