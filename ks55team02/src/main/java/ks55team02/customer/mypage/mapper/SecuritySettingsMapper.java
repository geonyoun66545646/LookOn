package ks55team02.customer.mypage.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SecuritySettingsMapper {

	/**
     * 주어진 사용자 번호(userNo)를 사용하여 특정 사용자의 현재 암호화된 비밀번호를 조회합니다.
     * 이 비밀번호는 주로 사용자가 입력한 현재 비밀번호와 일치하는지 확인하는 데 사용됩니다.
     *
     * @param userNo 조회할 사용자의 고유 회원 번호
     * @return     해당 사용자의 암호화된 비밀번호 문자열. 해당하는 사용자가 없을 경우 null을 반환합니다.
     */
    String selectUserEncryptedPasswordByNo(@Param("userNo") String userNo);

    /**
     * 주어진 사용자 번호(userNo)에 해당하는 사용자의 비밀번호를 새로운 암호화된 값으로 업데이트합니다.
     *
     * @param userNo            비밀번호를 업데이트할 사용자의 고유 회원 번호
     * @param newUserPswdEncptVal 새로 설정할 암호화된 비밀번호 값
     * @return                  데이터베이스에서 업데이트된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int updateUserPassword(
            @Param("userNo") String userNo,
            @Param("newUserPswdEncptVal") String newUserPswdEncptVal
    );

    /**
     * 특정 사용자(userNo)의 마지막 비밀번호 변경일시(last_pswd_chg_date)와
     * 마지막 보안 설정 수정일시(last_secu_set_mod_date)를 현재 시간으로 업데이트합니다.
     * 이 메소드는 {@code user_security_settings} 테이블에 관련 정보를 업데이트하거나 필요시 삽입합니다.
     *
     * @param userNo     정보를 업데이트할 사용자의 고유 회원 번호
     * @param updateTime 현재 비밀번호가 변경된 시점의 {@link LocalDateTime} 객체
     * @return           데이터베이스에서 업데이트되거나 삽입된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int updatePasswordChangeDate(
            @Param("userNo") String userNo,
            @Param("updateTime") LocalDateTime updateTime
    );
}
