package ks55team02.customer.post.domain;

import ks55team02.customer.login.domain.UserInfoResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Post {
    private String pstSn;
    private String bbsClsfCd;
    private String wrtrUserNo;
    private String pstTtl;
    private String pstCn;
    private int viewCnt;
    private Boolean ntcPstYn;
    private LocalDateTime crtDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime delDt;

    // --- 계산된 필드 ---
    private int cmntCnt;
    private int interCnt;
    private boolean likedByCurrentUser;

    // --- 연관 객체 ---
    private String bbsNm;
    private UserInfoResponse writerInfo; 
    private List<PostImage> imageList;
    private List<PostComment> commentList;
    private PostImage representativeImage;

    // --- 폼 제출용 필드 (DB와 무관) ---
    private List<MultipartFile> newImageFiles;
    private List<String> deleteImageSns;
    
    // 댓글작성제한
    private String cmntWrtAuthrtLvlVal;
}