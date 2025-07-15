package ks55team02.customer.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;

@Mapper
public interface InquiryMapper {

	// 문의 목록 조회
	List<Inquiry> getInquiryList();
	
	// 문의 상세 
	Inquiry getInquiryById(String inquiryId);
	
	// 문의 등록
	int addInquiry(Inquiry inquiry); 
	
	// 문의 이미지 매핑 정보 DB 삽입 (InquiryImageMapper로 이동)
	// int insertInquiryImage(InquiryImage inquiryImage); 
	
	// 여러 문의 이미지 매핑 정보를 데이터베이스에 삽입합니다. (InquiryImageMapper로 이동)
	// int addInquiryImages(List<InquiryImage> inquiryImageList);
	
	// 문의 ID로 문의 이미지 매핑 리스트를 조회합니다 (InquiryImageMapper로 이동)
	// List<InquiryImage> getInquiryImagesByInquiryId(String inqryId);
	
	// 조회 아이디중 가장 큰 값
	String getMaxInquiryId(); 
	
	// 전체 문의 개수 조회
    int getTotalInquiryCount();

    // 페이징 처리된 문의 목록 조회
    List<Inquiry> getInquiryListPaging(@Param("startRow") int startRow, @Param("pageSize") int pageSize);
    
    // 문의 상세조회시 이미지 정보 조회
    Inquiry getInquiryByIdWithImages(String inquiryId);
    
    Answer getAnswerByInquiryId(String answer);
    
}
