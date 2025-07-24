package ks55team02.admin.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.login.domain.AdminInfo;

@Mapper
public interface AdminMapper {

	/**
     * 주어진 관리자 아이디(adminId)를 사용하여 관리자의 상세 정보를 조회합니다.
     *
     * @param adminId 조회할 관리자의 로그인 아이디
     * @return        조회된 관리자 정보를 담은 {@link AdminInfo} 객체.
     * 해당 adminId에 해당하는 관리자가 없을 경우 null을 반환합니다.
     */
    AdminInfo getAdminInfoById(String adminId);
}
