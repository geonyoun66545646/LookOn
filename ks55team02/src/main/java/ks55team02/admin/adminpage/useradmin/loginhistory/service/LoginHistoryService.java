package ks55team02.admin.adminpage.useradmin.loginhistory.service;

import org.springframework.ui.Model;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;

public interface LoginHistoryService {
	
	void loadLoginHistory(LoginHistory criteria, Model model);
}
