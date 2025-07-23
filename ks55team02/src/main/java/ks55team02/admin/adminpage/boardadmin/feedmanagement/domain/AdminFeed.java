package ks55team02.admin.adminpage.boardadmin.feedmanagement.domain;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminFeed extends SearchCriteria {

    // feeds 테이블 컬럼
    private String feedSn;
    private String wrtrUserNo;
    private String feedCn;
    private LocalDateTime crtDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime delDt;
    private String delUserNo;
    
    // JOIN 또는 서브쿼리를 통해 가져올 추가 정보
    private String userNcnm;
    private String representativeImg;
    private int likeCount;
    private int commentCount;
    
    // 편의 필드
    private String feedStatus;
}