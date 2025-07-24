package ks55team02.customer.mypage.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserWithdrawalMapper {

	/**
     * 주어진 사용자 번호(userNo)에 해당하는 사용자의 개인 정보를 파기(예: NULL 처리 또는 마스킹)하고,
     * 해당 사용자의 계정 상태를 '탈퇴'로 변경합니다.
     * 이 메소드는 실제 데이터베이스의 사용자 테이블에 반영됩니다.
     *
     * @param userNo 탈퇴할 사용자의 고유 번호
     * @return     데이터베이스에서 업데이트된 레코드의 수 (성공 시 1, 실패 시 0)
     */
    int processUserWithdrawal(String userNo);

    /**
     * 주어진 사용자 번호(userNo)를 사용하여 해당 사용자의 암호화된 비밀번호를 조회합니다.
     * 이 비밀번호는 주로 회원 탈퇴 시 사용자가 입력한 비밀번호와 일치하는지 확인하는 데 사용됩니다.
     *
     * @param userNo 조회할 사용자의 고유 번호
     * @return     해당 사용자의 암호화된 비밀번호 문자열. 해당하는 사용자가 없을 경우 null을 반환합니다.
     */
    String getEncryptedPasswordByUserNo(String userNo);
}
