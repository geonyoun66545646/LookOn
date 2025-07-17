package ks55team02.admin.inquiry.service;

import java.util.List;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory; // InquiryAnswerHistory 임포트 추가

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

    // 문의 처리 상태 업데이트
    void updateInquiryProcessStatus(String inqryId, String prcsStts);
    
    // 문의 답변 이력 기록
    void insertInquiryAnswerHistory(InquiryAnswerHistory history); // 필요한 경우 추가
}