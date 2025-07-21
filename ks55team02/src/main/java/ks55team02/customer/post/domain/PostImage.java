package ks55team02.customer.post.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PostImage {
    private String pstImgSn;
    private String pstSn;
    private String imgFilePathNm;
    private int pstImgSortSn;     // 필드는 여기에 존재합니다.
    private String crtDt;
    private String delDt;
    private String delUserNo;



}