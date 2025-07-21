package ks55team02.customer.store.domain;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import lombok.Data;

@Data
public class ReviewAddDto {

    // ProductReview 객체와 필드명이 동일해야 자동으로 매핑됩니다.
    private String gdsNo;
    private String ordrNo;
    private int evlScr;
    private String reviewCn;

    // 파일 데이터를 받기 위한 필드
    // HTML의 <input name="reviewImages"> 와 이름이 같아야 합니다.
    private List<MultipartFile> reviewImages;
}