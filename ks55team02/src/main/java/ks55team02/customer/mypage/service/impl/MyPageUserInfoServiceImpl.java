package ks55team02.customer.mypage.service.impl;



import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.mypage.domain.UserUpdateRequest;
import ks55team02.customer.mypage.mapper.MyPageUserInfoMapper;
import ks55team02.customer.mypage.service.MyPageUserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageUserInfoServiceImpl implements MyPageUserInfoService{

	private final MyPageUserInfoMapper myPageUserInfoMapper;

	@Override
	@Transactional
	public String updateUserInfo(String userNo, UserUpdateRequest requestData) {
	    
	    // [1단계: 중복 검사]
	    // 이메일 필드에 값이 있고, 변경 요청이 들어왔을 때만 검사
	    if (requestData.getEmlAddr() != null && !requestData.getEmlAddr().isEmpty()) {
	        if (myPageUserInfoMapper.countByEmailForOthers(userNo, requestData.getEmlAddr()) > 0) {
	            // 문제가 생기면, 예외를 직접 던지고 메소드를 즉시 종료합니다. (catch하지 않습니다!)
	            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
	        }
	    }

	    // 연락처 필드에 값이 있고, 변경 요청이 들어왔을 때만 검사
	    if (requestData.getTelno() != null && !requestData.getTelno().isEmpty()) {
	        if (myPageUserInfoMapper.countByTelNoForOthers(userNo, requestData.getTelno()) > 0) {
	            // 문제가 생기면, 예외를 직접 던지고 메소드를 즉시 종료합니다. (catch하지 않습니다!)
	            throw new IllegalArgumentException("이미 사용 중인 연락처입니다.");
	        }
	    }

	    // [2단계: 업데이트 시도 및 결과 판별]
	    // 예외가 발생하지 않았을 경우에만 이 코드가 실행됩니다.
	    requestData.setUserNo(userNo);
	    int updatedRows = myPageUserInfoMapper.updateUserInfo(requestData);

	    if (updatedRows > 0) {
	        return "회원정보가 성공적으로 수정되었습니다.";
	    } else {
	        return "변경된 내용이 없습니다.";
	    }
	}
}
