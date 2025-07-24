package ks55team02.customer.register.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.register.domain.User;
import ks55team02.customer.register.domain.UserProfile;
import ks55team02.customer.register.domain.UserSecurity;

/**
 * 고객 회원가입과 관련된 데이터베이스 작업을 수행하는 매퍼 인터페이스입니다.
 * 사용자 정보, 보안 설정, 프로필 정보의 삽입 및 중복 확인 기능을 제공합니다.
 * MyBatis의 @Mapper 어노테이션을 사용하여 빈으로 등록됩니다.
 */
@Mapper
public interface CustomerRegisterMapper {

	/**
     * 주어진 사용자 로그인 아이디(userLgnId)의 중복 여부를 확인합니다.
     *
     * @param userLgnId 중복 여부를 검사할 로그인 아이디
     * @return          아이디가 이미 사용 중이면 true, 사용 가능하면 false를 반환합니다.
     */
    boolean idCheck(String userLgnId);

    /**
     * 주어진 사용자 닉네임(userNcnm)의 중복 여부를 확인합니다.
     *
     * @param userNcnm 중복 여부를 검사할 닉네임
     * @return         닉네임이 이미 사용 중이면 true, 사용 가능하면 false를 반환합니다.
     */
    boolean nicknameCheck(String userNcnm);
    
    /**
     * 주어진 이메일 주소(emlAddr)의 중복 여부를 확인합니다.
     *
     * @param emlAddr 중복 여부를 검사할 이메일 주소
     * @return        이메일 주소가 이미 사용 중이면 true, 사용 가능하면 false를 반환합니다.
     */
    boolean emailCheck(String emlAddr);
    
    /**
     * 주어진 전화번호(telno)의 중복 여부를 확인합니다.
     *
     * @param telno 중복 여부를 검사할 전화번호
     * @return      전화번호가 이미 사용 중이면 true, 사용 가능하면 false를 반환합니다.
     */
    boolean telnoCheck(String telno);

    /**
     * 데이터베이스에서 가장 최근에 생성된 사용자 번호(user_no)를 조회합니다.
     * 이는 새로운 사용자 등록 시 다음 user_no를 생성하는 데 활용될 수 있습니다.
     *
     * @return 가장 마지막에 사용된 user_no 문자열. 만약 데이터가 없을 경우 null 또는 기본값을 반환할 수 있습니다.
     */
    String getLastUserNo();

    /**
     * 새로운 사용자 정보를 `users` 테이블에 삽입합니다.
     *
     * @param user 삽입할 사용자 정보를 담은 {@link User} 도메인 객체
     */
    void addUser(User user);

    /**
     * 새로운 사용자의 보안 설정 정보를 `user_security_settings` 테이블에 삽입합니다.
     *
     * @param userSecurity 삽입할 사용자 보안 설정을 담은 {@link UserSecurity} 도메인 객체
     */
    void addUserSecurity(UserSecurity userSecurity);
    
    /**
     * 새로운 사용자의 프로필 정보를 `user_profile` 테이블에 삽입합니다.
     *
     * @param userProfile 삽입할 사용자 프로필 정보를 담은 {@link UserProfile} 도메인 객체
     */
    void addUserProfile(UserProfile userProfile);
}