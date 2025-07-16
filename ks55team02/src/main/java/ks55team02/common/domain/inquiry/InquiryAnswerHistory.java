package ks55team02.common.domain.inquiry; // Assuming common domain package

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryAnswerHistory {
    private String ansHstryId; // 답변 기록 ID (PK)
    private String answerId;   // 답변 ID (FK to answer table)
    private String inqryId;    // 문의 ID (FK to inquiries table, for reference)
    private LocalDateTime chgDt; // 변경 일시
    private String prcsUserNo; // 처리 사용자 번호 (FK to users table)
    private String chgBfrStts; // 변경 전 상태
    private String chgAftrStts; // 변경 후 상태
    private String chgCn;      // 변경 내용/사유 (예: "답변 등록", "답변 수정")
}