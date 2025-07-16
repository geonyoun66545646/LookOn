package ks55team02.seller.inquiry.service;

import java.util.List;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;

public interface SellerInquiryService {
	
	int getSellerInquiryCnt(Inquiry inquiry);
	
	List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize);

	Inquiry getSellerInquiryByStoreId(String inqryId);

	Answer registerAnswer(String inqryId, String ansCn, String answrUserNo);

	Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo);

	// 상점 ID로 판매자 사용자 번호 조회 메서드 추가
	String getSellerUserNoByStoreId(String storeId);
}