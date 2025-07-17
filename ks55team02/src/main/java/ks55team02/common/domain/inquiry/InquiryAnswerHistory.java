package ks55team02.common.domain.inquiry;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class InquiryAnswerHistory {
    private String ansHstryId;
    private String answerId;
    private String inqryId;
    private LocalDateTime chgDt;
    private String prcsUserNo;
    private String chgBfrStts;
    private String chgAftrStts;
    private String chgCn;

    

    @Override
    public String toString() {
        return "InquiryAnswerHistory{" +
               "ansHstryId='" + ansHstryId + '\'' +
               ", answerId='" + answerId + '\'' +
               ", inqryId='" + inqryId + '\'' +
               ", chgDt=" + chgDt +
               ", prcsUserNo='" + prcsUserNo + '\'' +
               ", chgBfrStts='" + chgBfrStts + '\'' +
               ", chgAftrStts='" + chgAftrStts + '\'' +
               ", chgCn='" + chgCn + '\'' +
               '}';
    }
}