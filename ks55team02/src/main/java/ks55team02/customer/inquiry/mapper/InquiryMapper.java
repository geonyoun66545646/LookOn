package ks55team02.customer.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.inquiry.domain.Inquiry;

@Mapper
public interface InquiryMapper {

	// 문의 목록 조회
	List<Inquiry> getInquiryList();
	
	int getInquiryCount();
		
	// 문의 상세 
	Inquiry getInquiryById(String inquiryId);
	
	// 문의 등록
	int addInquiry(Inquiry inquiry); 
	
	// 조회 아이디중 가장 큰 값
	String getMaxInquiryId(); 

}