package ks55team02.customer.search.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostsSearch {
	private String pstSn;
    private String pstTtl;
    private String pstCn;
    private String userNcnm;
    private LocalDateTime crtDt;
}
