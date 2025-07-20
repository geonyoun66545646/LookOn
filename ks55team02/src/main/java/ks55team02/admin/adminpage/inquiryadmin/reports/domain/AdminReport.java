package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 관리자 신고 목록/상세 조회 시 사용될 DTO
 */
@Data
public class AdminReport {

    // 1. reports 테이블의 기본 정보
    private String dclrId;
    private String dclrTrgtTypeCd;      // 신고 대상 타입 코드 (U:사용자, P:상품, S:상점 등)
    private String prcsSttsCd;          // 처리 상태 코드 (e.g., 접수, 처리중, 완료)
    private LocalDateTime dclrRcptDt;   // 신고 접수 일시

    // 2. JOIN을 통해 가져올 추가 정보 (목록 화면 표시용)
    private String dclrUserNickname;    // 신고자 닉네임 (users 테이블 JOIN)
    private String dclrReasonContent;   // 신고 사유 내용 (report_reasons 테이블 JOIN)

    // '지능형 대상 정보'를 담을 필드
    private String intelligentTargetInfo; // (예: "상품: A상품", "사용자: B닉네임")

    // 3. 상세 페이지에서 사용할 추가 정보 (필요시 확장)
    private String dtlDclrRsnCn;        // 상세 신고 사유
    private String dclrTrgtUserNo;      // 신고 대상 사용자 번호
    private String dclrTrgtContsId;     // 신고 대상 콘텐츠 ID
    private LocalDateTime prcsCmptnDt;  // 처리 완료 일시
    private String dclrPrcsRsltCn;      // 신고 처리 결과 내용
}