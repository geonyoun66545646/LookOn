package ks55team02.common.domain.store;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewLike {

    private String likeId;         	// 좋아요 ID (like_id)
    private String reviewId;       	// 리뷰 ID (review_id)
    private String likeUserNo;     	// 좋아요 누른 사용자 번호 (like_user_no)
    private LocalDateTime likeYmd; 	// 좋아요 날짜 (like_ymd)
    private Boolean likeYn;        	// 좋아요 여부 (TRUE: 좋아요, FALSE: 취소) (like_yn)
    private LocalDateTime rtrcnDt; 	// 취소일시 (rtrcn_dt)
}