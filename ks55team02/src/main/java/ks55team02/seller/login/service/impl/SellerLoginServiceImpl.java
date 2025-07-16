package ks55team02.seller.login.service.impl;

import org.springframework.stereotype.Service;

import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.login.domain.SellerInfo;
import ks55team02.seller.login.domain.SellerLoginRequest;
import ks55team02.seller.login.mapper.SellerLoginMapper;
import ks55team02.seller.login.service.SellerLoginService;
import ks55team02.systems.crypto.utils.PasswordEncryptor;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerLoginServiceImpl implements SellerLoginService{

	// 새로 만든 SellerMapper와, 기존 암호화 유틸을 주입받습니다.
    private final SellerLoginMapper sellerLoginMapper;
    private final PasswordEncryptor passwordEncryptor;

    @Override
    public LoginUser login(SellerLoginRequest loginRequest) {
        
        // 1. 아이디로 DB에서 사용자 정보를 가져옵니다.
        SellerInfo sellerInfo = sellerLoginMapper.getSellerInfoById(loginRequest.getSellerId());

        // 2. 아이디가 존재하고, 비밀번호가 일치하는지 확인합니다.
        if (sellerInfo != null && passwordEncryptor.checkPassword(loginRequest.getSellerPw(), sellerInfo.getUserPswd())) {
            
            String userGrade = sellerInfo.getMbrGrdCd();
            // 3. (가장 중요) 등급이 '판매자'인지 확인합니다. (DB 등급 코드: grd_cd_2)
            if ("grd_cd_2".equals(userGrade)) {
                
                // 4. 로그인 성공 시, 세션에 저장할 LoginUser 객체를 만들어 반환합니다.
                //    LoginUser는 여러 역할이 공통으로 사용할 수 있는 좋은 객체입니다.
                return new LoginUser(
                        sellerInfo.getUserNo(),
                        sellerInfo.getMbrGrdCd(),
                        sellerInfo.getUserLgnId(),
                        sellerInfo.getUserNm()
                );
            }
        }

        // 로그인에 실패하면 null을 반환합니다.
        return null;
    }
}
