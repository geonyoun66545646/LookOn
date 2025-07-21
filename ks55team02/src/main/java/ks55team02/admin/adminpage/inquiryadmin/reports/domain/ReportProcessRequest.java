package ks55team02.admin.adminpage.inquiryadmin.reports.domain; // <-- 정확한 패키지 경로

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportProcessRequest {
    
	private String hstryId;
    private String dclrId;
    private String prcsCn;
    private String newStatus;
    private String adminId; 
    private String dclrPrcsRsltCn;
    private String prcsDsctnMemoCn;
    
}