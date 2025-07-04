package ks55team02.customer.register.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.register.domain.User;
import ks55team02.customer.register.domain.UserProfile;
import ks55team02.customer.register.domain.UserSecurity;

@Mapper
public interface CustomerRegisterMapper {

	// 아이디 중복 확인 (단순 타입 boolean 반환)
    boolean idCheck(String userLgnId);

    // 닉네임 중복 확인 (단순 타입 boolean 반환)
    boolean nicknameCheck(String userNcnm);
    
    // 이메일 중복 확인 (단순 타입 boolean 반환)
    boolean emailCheck(String emlAddr);
    
    // 전화번호 중복 확인 (단순 타입 boolean 반환)
    boolean telnoCheck(String telno);

    // 가장 마지막 user_no 조회 (단순 타입 String 반환)
    String getLastUserNo();

    // USER 테이블에 회원 정보 삽입
    void addUser(User user);

    // USER_SECURITY 테이블에 회원 보안 정보 삽입
    void addUserSecurity(UserSecurity userSecurity);
    
    // USER_PROFILE 테이블에 회원 프로필 정보 삽입
    void addUserProfile(UserProfile userProfile);
    
}
