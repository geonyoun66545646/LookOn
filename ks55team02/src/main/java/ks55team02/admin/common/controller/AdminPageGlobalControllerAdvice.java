package ks55team02.admin.common.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice(basePackages = {"ks55team02.admin.adminpage", "ks55team02.admin.inquiry"}) 
public class AdminPageGlobalControllerAdvice {

	@ModelAttribute("activeMenu")
    public String addActiveMenu(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        if (requestURI.contains("/adminpage/useradmin/")) {
            return "user";
        } else if (requestURI.contains("/adminpage/storeadmin/")) {
            return "store";
        } else if (requestURI.contains("/adminpage/productadmin/")) {
            return "product";
        } else if (requestURI.contains("/adminpage/boardadmin/")) {
            return "board";
        } else if (requestURI.contains("/adminpage/inquiryadmin/")) {
            return "inquiry";
		} else if (requestURI.contains("/admin/inquiry/")) {
			return "inquiry";
		}
        
        return "adminpage";
    }
}
