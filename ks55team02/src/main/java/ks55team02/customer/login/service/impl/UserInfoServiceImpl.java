package ks55team02.customer.login.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.login.domain.UserInfoResponse;
import ks55team02.customer.login.mapper.UserInfoMapper;
import ks55team02.customer.login.service.UserInfoService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService{

	private final UserInfoMapper userInfoMapper;
	
	@Override
	public UserInfoResponse getUserInfo(String userNo) {
		
		// 단순히 Mapper의 메소드를 호출하여 결과를 그대로 반환합니다.
        UserInfoResponse userInfo = userInfoMapper.findUserInfoByUserNo(userNo);
		
		return userInfo;
	}
}
