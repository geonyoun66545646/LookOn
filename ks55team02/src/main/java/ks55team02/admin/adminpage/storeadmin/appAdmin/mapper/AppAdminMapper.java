package ks55team02.admin.adminpage.storeadmin.appAdmin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.storeadmin.appAdmin.domain.AppAdmin;

@Mapper
public interface AppAdminMapper {

	List<AppAdmin> getAppAdminList();
	
}
