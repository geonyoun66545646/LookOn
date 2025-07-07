package ks55team02.admin.adminpage.useradmin.loginhistory.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;

@Mapper
public interface LoginHistoryMapper {

	List<LoginHistory> getLoginHistoryList(LoginHistory criteria);
	
    int getLoginHistoryCount(LoginHistory criteria);
}
