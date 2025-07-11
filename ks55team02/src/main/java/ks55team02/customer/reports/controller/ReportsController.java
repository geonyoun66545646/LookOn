package ks55team02.customer.reports.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/customer")
public class ReportsController {

	@GetMapping("/reports")
    public String customerReports() {
        return "customer/reports/reports"; // templates/customer/reports/reports.html
    }
	
	@GetMapping("/myReports")
    public String customerMyReports() {
        return "customer/reports/myReports"; // templates/customer/reports/myReports.html
    }
	
}