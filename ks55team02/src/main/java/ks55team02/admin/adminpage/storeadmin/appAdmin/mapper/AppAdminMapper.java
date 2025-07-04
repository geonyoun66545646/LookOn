package ks55team02.admin.adminpage.storeadmin.appAdmin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;

@Mapper
public interface AppAdminMapper {

	// 전체 신청 조회
	List<AppAdmin> getAppAdminList();
	
}
