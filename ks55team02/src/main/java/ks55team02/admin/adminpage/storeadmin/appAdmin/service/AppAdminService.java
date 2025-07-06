package ks55team02.admin.adminpage.storeadmin.appAdmin.service;

import java.util.List;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;

public interface AppAdminService {
	
	List<AppAdmin> getAppAdminList(); 

	// 상점 신청 상세, 신청ID로 정보를 조회
	AppAdmin getAppAdminById(String aplyId);
}
