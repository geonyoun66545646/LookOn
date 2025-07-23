package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminReportHistory {

	private String hstryId;
	private String dclrId;
	private String userNo;
	private String prcsUserNcnm;
	private String actnCn;
	private String bfrSttsCd;
	private String crntSttsCd;
	private String prcsDt;
	private String prcsDsctnMemoCn;

	// 이 필드는 더 이상 user_sanctions와 조인하지 않으므로, 아래 actionType으로 통합하는 것이 좋습니다.
	private String sanctionType;

	// [수정] 보이지 않는 특수 문자를 제거한 라인입니다.
	private String actionType;
}