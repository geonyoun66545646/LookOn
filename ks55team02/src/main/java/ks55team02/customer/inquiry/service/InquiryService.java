package ks55team02.customer.inquiry.service;



import java.util.List;

import ks55team02.customer.inquiry.domain.Inquiry;

public interface InquiryService {

	// 문의 목록 조회
	List<Inquiry> getInquiryList();
	
	// 문의 상세 
	Inquiry getInquiryById(String inquiryId);
	
	//문의 등록
	void addInquiry(Inquiry inquiry);
	
}
