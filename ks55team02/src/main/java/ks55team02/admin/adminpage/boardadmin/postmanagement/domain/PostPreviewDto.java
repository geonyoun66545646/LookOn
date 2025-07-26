package ks55team02.admin.adminpage.boardadmin.postmanagement.domain;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PostPreviewDto {
    private String postSn;
    private String postTitle;
    private String postContent;
    private LocalDateTime createdDate;
    private String userNickname;
    private String boardName;
    private int viewCount;
    private int likeCount;
    private int commentCount;
    private List<String> imageUrls; // 게시글에 포함된 모든 이미지 경로 리스트
}