package ks55team02.seller.inquiry.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.seller.inquiry.mapper.SellerInquiryMapper;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class SellerInquiryServiceImpl implements SellerInquiryService{
	
	private final SellerInquiryMapper sellerInquiryMapper;
	
	@Override
	public int getSellerInquiryCnt(Inquiry inquiry) {
		log.info("서비스: getSellerInquiryCnt 호출 - 검색 조건: {}", inquiry);
		return sellerInquiryMapper.getSellerInquiryCnt(inquiry);
	}
	@Override
	public List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize) {
		log.info("서비스: getSellerInquiryList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", inquiry, limitStart, pageSize); //
		 // 매퍼 호출 시 limitStart와 pageSize를 함께 전달
		return sellerInquiryMapper.getSellerInquiryList(inquiry,limitStart,pageSize);
	}
	
	@Override
	public Inquiry getSellerInquiryByStoreId(String inqryId) {
		
		return sellerInquiryMapper.getSellerInquiryByStoreId(inqryId);
	}

}
