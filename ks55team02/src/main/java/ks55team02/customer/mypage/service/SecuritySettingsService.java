package ks55team02.customer.mypage.service;

import ks55team02.customer.mypage.domain.PasswordChangeRequest;

public interface SecuritySettingsService {

	/**
     * 사용자의 비밀번호를 변경합니다.
     * 현재 비밀번호 확인, 새 비밀번호 유효성 검사 및 암호화를 수행한 후 데이터베이스를 업데이트합니다.
     *
     * @param userNo 현재 로그인된 사용자의 회원 번호 (세션에서 가져옴)
     * @param request 비밀번호 변경 요청 DTO (현재 비밀번호, 새 비밀번호, 확인 비밀번호 포함)
     * @return 비밀번호 변경 성공 여부 (true: 성공, false: 실패)
     * @throws IllegalArgumentException 현재 비밀번호 불일치, 새 비밀번호 불일치 등 비즈니스 로직 오류 시 발생
     */
    boolean changePassword(String userNo, PasswordChangeRequest request);
}
