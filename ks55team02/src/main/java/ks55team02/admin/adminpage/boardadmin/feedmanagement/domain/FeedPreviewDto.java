package ks55team02.admin.adminpage.boardadmin.feedmanagement.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class FeedPreviewDto {
    private String feedSn;
    private String feedCn;
    private LocalDateTime crtDt;
    private String userNcnm;
    private int likeCount;
    private int commentCount;
    private List<String> imageUrls; // 피드에 포함된 모든 이미지 경로 리스트
    private List<String> tags;      // 해시태그 리스트
}