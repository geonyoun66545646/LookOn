package ks55team02.seller.inquiry.service;

import java.util.List;

import ks55team02.common.domain.inquiry.Inquiry;

public interface SellerInquiryService {
	
	List<Inquiry> getSellerInquiryList(String StoreId);

}
