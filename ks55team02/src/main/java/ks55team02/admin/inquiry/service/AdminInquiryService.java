package ks55team02.admin.inquiry.service;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory;

import java.util.List;

public interface AdminInquiryService {

    // 관리자 전체 문의 개수 조회
    int getAdminInquiryCnt(Inquiry inquiry);

    // 관리자 전체 문의 목록 조회
    List<Inquiry> getAdminInquiryList(Inquiry inquiry, int limitStart, int pageSize);

    // 문의 상세 조회 (Admin 용도)
    Inquiry getInquiryDetailById(String inqryId);

    // 답변 등록
    Answer registerAnswer(String inqryId, String ansCn, String answrUserNo);

    // 답변 수정
    Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo);
    
    // 문의 답변 이력 기록
    void insertInquiryAnswerHistory(InquiryAnswerHistory history);

    /**
     * [신규 추가] 여러 문의의 상태를 일괄적으로 변경하는 메소드
     * @param inquiryIds 상태를 변경할 문의 ID 목록
     * @param status     변경할 상태 (예: "접수", "완료", "삭제")
     * @param adminUserNo 작업을 수행하는 관리자의 사용자 번호
     */
    void updateInquiryStatus(List<String> inquiryIds, String status, String adminUserNo);
    
    // [삭제 또는 주석 처리] 기존 메소드는 새로운 메소드로 대체됩니다.
    // void updateInquiryProcessStatus(String inqryId, String prcsStts);
}