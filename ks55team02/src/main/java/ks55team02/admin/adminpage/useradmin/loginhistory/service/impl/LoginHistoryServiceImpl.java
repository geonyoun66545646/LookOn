package ks55team02.admin.adminpage.useradmin.loginhistory.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import ks55team02.admin.adminpage.useradmin.loginhistory.domain.LoginHistory;
import ks55team02.admin.adminpage.useradmin.loginhistory.mapper.LoginHistoryMapper;
import ks55team02.admin.adminpage.useradmin.loginhistory.service.LoginHistoryService;
import ks55team02.admin.common.domain.Pagination;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

	private final LoginHistoryMapper loginHistoryMapper;
	
	@Override
	public void loadLoginHistory(LoginHistory criteria, Model model) {
        int totalCount = loginHistoryMapper.getLoginHistoryCount(criteria);
        
        // ✨ 공용 Pagination DTO 사용!
        Pagination pagination = new Pagination(totalCount, criteria);
        criteria.setOffset(pagination.getLimitStart());
        
        List<LoginHistory> loginHistoryList = loginHistoryMapper.getLoginHistoryList(criteria);
        
        model.addAttribute("loginHistoryList", loginHistoryList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pagination", pagination);
        model.addAttribute("searchCriteria", criteria);
    }
}
