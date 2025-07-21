package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminReportHistory {

	// report_history 테이블 정보
	private String hstryId; // 처리 이력 아이디 (hstry_id)
	private String dclrId; // 신고 아이디 (dclr_id)
	private String prcsUserNo; // 처리자 회원 번호 (user_no)
	private String prcsUserNcnm; // 처리자 닉네임 (users.user_ncnm)
	private String prcsCn; // 처리 내용 (prcs_cn)
	private String bfrSttsCd; // 이전 상태 코드 (bfr_stts_cd)
	private String crntSttsCd; // 현재 상태 코드 (crnt_stts_cd)
	private String prcsDt; // 처리 일자 (prcs_dt)
}