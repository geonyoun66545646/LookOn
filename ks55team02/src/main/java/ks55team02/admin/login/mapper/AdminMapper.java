package ks55team02.admin.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.login.domain.AdminInfo;

@Mapper
public interface AdminMapper {

	/**
     * 관리자 아이디로 관리자 정보를 조회합니다.
     * @param adminId 관리자 아이디
     * @return 관리자 정보 DTO
     */
    AdminInfo getAdminInfoById(String adminId);
}
