package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminReportDetail {

    // reports 테이블 정보
    private String dclrId;
    private String dclrUserNo;
    private String dclrUserNcnm;
    private String dclrTrgtUserNo;
    private String dclrTrgtUserNcnm;
    private String dclrRsnCd;
    private String dclrRsnCn;
    private String dclrCn;
    private String dclrDt;
    private String prcsSttsCd;
    
    // report_attachments 테이블 정보
    private int attachmentCount;

    // 1:N 관계
    private List<AdminReportHistory> historyList; 

    // 처리 완료 후 정보
    private String prcsPicMngrNo;
    private String dclrPrcsRsltCn;
    
    // [수정 3] 유효성 검사에 필요한 필드들을 추가합니다.
    private String dclrTrgtTypeCd; // 신고 대상 타입
    private String dclrTrgtContsId; // 신고 대상 콘텐츠 ID
    
    private String prcsCmptnDt; // 처리 완료 일시

}