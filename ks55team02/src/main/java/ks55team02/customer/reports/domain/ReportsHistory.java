package ks55team02.customer.reports.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportsHistory {

	private String hstryId;
	private String dclrId;
	private String userNo;
	private String actnCn;
	private String bfrSttsCd;
	private String crntSttsCd;
	private LocalDateTime prcsDt;
}