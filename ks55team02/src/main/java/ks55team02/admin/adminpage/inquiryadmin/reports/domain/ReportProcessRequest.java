package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime; // 1. LocalDateTime import 추가

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReportProcessRequest {

    // --- 기존 필드들 ---
    private String dclrId;
    private String newStatus;
    private String dclrPrcsRsltCn;
    private String adminId; // 처리 담당 관리자 ID
    
    // 이력 기록용 필드
    private String hstryId;
    private String prcsCn; // 조치 내용
    private String prcsDsctnMemoCn; // 내부 처리 메모
    private String sanctionType; // 제재 유형
    private String sanctionDuration; // 제재 기간
    
    
    // ★★★★★★★★★★ 이 필드를 추가하세요 ★★★★★★★★★★
    private LocalDateTime prcsCmptnDt; // 처리 완료 일시
    // ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

    // Lombok의 @Getter, @Setter 어노테이션이 있으므로
    // 별도로 getPrcsCmptnDt() 와 setPrcsCmptnDt() 메소드를 만들 필요가 없습니다.
}