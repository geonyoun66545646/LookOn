package ks55team02.seller.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory; // 추가

@Mapper
public interface SellerInquiryMapper {

	int getSellerInquiryCnt(Inquiry inquiry);
		
	List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize);
	
	Inquiry getSellerInquiryByStoreId(String inqryId);

	// 상점 ID로 판매자 사용자 번호 조회
	String getSellerUserNoByStoreId(String storeId);
	
	// 문의 처리 상태 업데이트 메서드 추가
	int updateInquiryProcessStatus(String inqryId, String prcsStts);

	// 문의 답변 이력 기록 메서드 추가
	int insertInquiryAnswerHistory(InquiryAnswerHistory history);

	// ans_hstry_id 중 최대 숫자 부분 조회
	Integer getMaxAnsHstryIdNumber();

	// 문의의 현재 처리 상태 조회
	String getInquiryProcessStatus(String inqryId);
}