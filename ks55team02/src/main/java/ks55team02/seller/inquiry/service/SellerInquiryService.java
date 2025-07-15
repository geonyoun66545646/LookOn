package ks55team02.seller.inquiry.service;

import java.util.List;

import ks55team02.common.domain.inquiry.Inquiry;

public interface SellerInquiryService {
	
	int getSellerInquiryCnt(Inquiry inquiry);
	
	List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize);

	Inquiry getSellerInquiryByStoreId(String inqryId);
}
