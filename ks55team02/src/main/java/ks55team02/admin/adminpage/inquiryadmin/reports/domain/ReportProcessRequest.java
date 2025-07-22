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
    
 // [추가] 제재 종류를 담을 필드 (예: "7일 이용 정지")
    private String sanctionType;
    
}