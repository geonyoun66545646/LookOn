package ks55team02.admin.adminpage.storeadmin.appAdmin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;
import ks55team02.admin.common.domain.SearchCriteria;

@Mapper
public interface AppAdminMapper {
	
	// 신청 상세 아이디 조회
	AppAdmin getAppAdminById(String aplyId);
	
	// AppAdmin 목록의 전체 개수를 가져오는 메서드
	public int getAppAdminCount(AppAdmin appAdmin);
	
	// 전체 개수 조회
	List<AppAdmin> getAppAdminList(
	        @Param("appAdmin") AppAdmin appAdmin,
	        @Param("limitStart") int limitStart,
	        @Param("pageSize") int pageSize
	);

}
