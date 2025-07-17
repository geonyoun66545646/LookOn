package ks55team02.common.domain.inquiry;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class StoreImages {
	public class StoreImage {
	    private String imgId;         // 이미지 ID
	    private String imgAddr;       // 이미지 경로
	    private String imgFileNm;     // 이미지 파일명 (원본 파일명)
	    private Long imgFileSz;       // 이미지 파일 크기 (KB)
	    private String imgTypeCd;     // 이미지 유형 코드 (예: PNG, JPG)
	    private LocalDateTime regYmd; // 등록일
	    private boolean delYn;        // 삭제 여부 (0: 미삭제, 1: 삭제)

}
}
