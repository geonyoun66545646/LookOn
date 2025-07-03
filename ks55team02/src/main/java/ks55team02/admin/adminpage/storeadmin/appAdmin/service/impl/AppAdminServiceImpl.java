package ks55team02.admin.adminpage.storeadmin.appAdmin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;
import ks55team02.admin.adminpage.storeadmin.appAdmin.mapper.AppAdminMapper;
import ks55team02.admin.adminpage.storeadmin.appAdmin.service.AppAdminService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AppAdminServiceImpl implements AppAdminService{
	
	private final AppAdminMapper appAdminMapper;
	
	// 신청 목록 조회
	@Override
	public List<AppAdmin> getAppAdminList() {
		List<AppAdmin> appAdminList = appAdminMapper.getAppAdminList();
		
		return appAdminList;
	}

}
