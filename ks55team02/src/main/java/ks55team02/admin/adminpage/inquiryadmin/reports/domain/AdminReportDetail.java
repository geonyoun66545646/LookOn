package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.util.List; // 1:N 관계를 위해 List import

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminReportDetail {

    // reports 테이블 정보
    private String dclrId;          // 신고 아이디 (dclr_id)
    private String dclrUserNo;      // 신고자 회원 번호 (dclr_user_no)
    private String dclrUserNcnm;    // 신고자 닉네임 (users.user_ncnm)
    private String dclrTrgtUserNo;  // 신고대상 회원 번호 (dclr_trgt_user_no)
    private String dclrTrgtUserNcnm;// 신고대상 닉네임 (users.user_ncnm)
    private String dclrRsnCd;       // 신고 사유 코드 (dclr_rsn_cd)
    private String dclrRsnCn;       // 신고 사유 내용 (report_reasons.dclr_rsn_cn)
    private String dclrCn;          // 신고 상세 내용 (dclr_cn)
    private String dclrDt;          // 신고 일자 (dclr_dt)
    private String prcsSttsCd;      // 처리 상태 코드 (prcs_stts_cd)
    
    // report_attachments 테이블 정보 (우선 파일 개수만)
    private int attachmentCount;    // 첨부파일 개수

    // 1:N 관계: 하나의 신고 상세 정보는 여러 처리 이력을 가질 수 있습니다.
    private List<AdminReportHistory> historyList; 
}