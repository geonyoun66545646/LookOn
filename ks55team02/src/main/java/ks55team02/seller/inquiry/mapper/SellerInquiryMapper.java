package ks55team02.seller.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;

@Mapper
public interface SellerInquiryMapper {

	int getSellerInquiryCnt(Inquiry inquiry);
		
	List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize);
	
	Inquiry getSellerInquiryByStoreId(String inqryId);

	// 상점 ID로 판매자 사용자 번호 조회
	String getSellerUserNoByStoreId(String storeId);
}