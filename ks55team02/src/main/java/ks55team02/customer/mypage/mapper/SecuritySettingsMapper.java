package ks55team02.customer.mypage.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SecuritySettingsMapper {

	/**
     * 특정 사용자의 암호화된 비밀번호를 조회합니다.
     *
     * @param userNo 조회할 사용자의 회원번호
     * @return 해당 사용자의 암호화된 비밀번호 문자열
     */
    String selectUserEncryptedPasswordByNo(@Param("userNo") String userNo);

    /**
     * 특정 사용자의 비밀번호를 새로운 암호화된 값으로 업데이트합니다.
     *
     * @param userNo 업데이트할 사용자의 회원번호
     * @param newUserPswdEncptVal 새로 암호화된 비밀번호 값
     * @return 업데이트된 행의 수
     */
    int updateUserPassword(
            @Param("userNo") String userNo,
            @Param("newUserPswdEncptVal") String newUserPswdEncptVal
    );

    /**
     * 특정 사용자의 마지막 비밀번호 변경일시와 마지막 보안 설정 수정일시를 업데이트합니다.
     * (user_security_settings 테이블 업데이트 또는 삽입)
     *
     * @param userNo 업데이트할 사용자의 회원번호
     * @param updateTime 현재 시간 (LocalDateTime)
     * @return 업데이트된/삽입된 행의 수
     */
    int updatePasswordChangeDate(
            @Param("userNo") String userNo,
            @Param("updateTime") LocalDateTime updateTime
    );
}
