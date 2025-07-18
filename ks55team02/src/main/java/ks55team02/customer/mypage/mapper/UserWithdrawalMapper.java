package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserWithdrawalMapper {

	/**
     * 사용자의 개인정보를 파기하고 탈퇴 상태로 변경합니다.
     * @param userNo 탈퇴할 사용자의 고유 번호
     * @return 영향을 받은 행의 수
     */
    int processUserWithdrawal(String userNo);

    /**
     * 사용자의 암호화된 비밀번호를 조회합니다.
     * @param userNo 조회할 사용자의 고유 번호
     * @return 암호화된 비밀번호 문자열
     */
    String getEncryptedPasswordByUserNo(String userNo);
}
