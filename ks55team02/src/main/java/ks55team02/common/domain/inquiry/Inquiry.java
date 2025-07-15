package ks55team02.common.domain.inquiry;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class Inquiry extends SearchCriteria{
	private String 			inqryId;
	private String 			inqryTypeCd;
	private String 			inqryTrgtTypeCd;
	private String 			wrtrId;
	private String 			inqryStoreId;
	private String 			inqryTtl;
	private String 			inqryCn;
	private boolean			prvtYn;
	private String 			prcsStts;
	private String 			prcsUserNo;
	private LocalDateTime 	regYmd;
	private LocalDateTime  	mdfcnYmd;
	private LocalDateTime  	delDt;
	private String 	  		delUserNo;
	private boolean 		delActvtnYn;
	// InquiryUser 타입의 객체를 writerInfo 로 받음.
	private InquiryUser  	writerInfo ;
	// Answer 타입의 객체를 answer 로 받음.
	private Answer 			answer;
	
	// 정렬을 위한 키
	private String 			sortKey; 			// 정렬 기준 컬럼 (예: "aplyId", "ctrtAplyYmd")
    private String 			sortOrder; 			// 정렬 방향 (예: "ASC", "DESC")

	   // inq_ 제거
    public String getInqryIdNum() {
        if (this.inqryId == null) {
            return null;
        }
        // 'inq_' 접두사를 제거하고 숫자 부분만 반환
        return this.inqryId.replaceFirst("inq_", "");
    }
 // 문의에 연결된 이미지 매핑 정보 (MyBatis에서 1:N으로 조회할 때 사용)
    private List<InquiryImage> inquiryImages = new ArrayList<>();
    
 
    
}