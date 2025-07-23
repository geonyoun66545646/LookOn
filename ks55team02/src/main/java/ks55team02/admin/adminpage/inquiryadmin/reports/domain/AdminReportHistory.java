package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminReportHistory {

	private String hstryId; // hstry_id
	private String dclrId; // dclr_id
	private String userNo; // user_no (처리 사용자 번호)

	// 이 필드는 user 테이블과 조인해서 가져와야 하므로 그대로 둡니다.
	private String prcsUserNcnm; // 처리자 닉네임 (조인 결과)

	private String actnCn; // actn_cn (조치 내용)
	private String bfrSttsCd; // bfr_stts_cd
	private String crntSttsCd; // crnt_stts_cd
	private String prcsDt; // prcs_dt
	private String prcsDsctnMemoCn; // prcs_dsctn_memo_cn

	// user_sanctions 테이블에서 조인으로 가져올 필드
	private String sanctionType;
}