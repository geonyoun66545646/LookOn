package ks55team02.customer.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.login.domain.UserInfoResponse;

@Mapper
public interface UserInfoMapper {

	UserInfoResponse findUserInfoByUserNo(String userNo);
}
