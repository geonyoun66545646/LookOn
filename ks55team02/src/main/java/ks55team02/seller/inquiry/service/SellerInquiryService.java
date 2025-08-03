package ks55team02.seller.inquiry.service;

import java.util.List;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory; // 추가

public interface SellerInquiryService {

    // 판매자 문의 전체 개수 조회
    int getSellerInquiryCnt(Inquiry inquiry);

    // 판매자 문의 목록 조회
    List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize);

    // 문의 상세 조회 (상점 ID 기준)
    Inquiry getSellerInquiryByStoreId(String inqryId);
    
    // 상점 ID로 판매자 사용자 번호 조회
    String getSellerUserNoByStoreId(String storeId);

    // 문의 답변 등록
    Answer registerAnswer(String inqryId, String ansCn, String answrUserNo);

    // 문의 답변 수정
    Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo);

    // ⭐ 추가: 로그인 ID로 상점 ID 조회
    String getStoreIdByUserLgnId(String userLgnId);
    
}