package ks55team02.seller.inquiry.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.seller.inquiry.mapper.SellerInquiryMapper;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class SellerInquiryServiceImpl implements SellerInquiryService {
	
	private final SellerInquiryMapper sellerInquiryMapper;
	
	@Override
	public List<Inquiry> getSellerInquiryList(String StoreId) {
		List<Inquiry> SellerinquiryList = sellerInquiryMapper.getSellerInquiryList(StoreId);
		return SellerinquiryList;
	}

}
