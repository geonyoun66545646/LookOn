package ks55team02.customer.store.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class ProductReview {
	private String reviewId;            // 리뷰 ID (review_id)
	private String ordrDtlArtclNo;      // 주문 상세 항목 ID (ordr_dtl_artcl_no)
	private String prchsrUserNo;        // 구매자 ID (prchsr_user_no)
	private String gdsNo;               // 상품 ID (gds_no)
	private String ordrNo;              // 주문 ID (ordr_no)
	private Integer evlScr;             // 평점 (evl_scr) - int로 변경 (DEFAULT NULL)
	private String reviewCn;            // 리뷰 내용 (review_cn)
	private LocalDateTime wrtYmd;       // 작성일 (wrt_ymd) - datetime으로 변경
	private String reviewStts;          // 리뷰 상태 (review_stts)
	private LocalDateTime lastMdfcnDt;  // 최종 수정일시 (last_mdfcn_dt) - datetime으로 변경
	private LocalDateTime delDt;        // 삭제(숨김) 일시 (del_dt) - datetime으로 변경
	private String delPrcrNo;           // 삭제(숨김) 처리자 번호 (del_prcr_no)
	private Integer helpdCnt;           // 도움돼요 수 (helpd_cnt) - int로 변경 (DEFAULT '0')
	private String starRating;          // star_rating

	private List<ReviewImage> reviewImages; // 리뷰 이미지 목록

}

