package ks55team02.admin.adminpage.boardadmin.postmanagement.domain;

import java.time.LocalDateTime;

import ks55team02.admin.common.domain.SearchCriteria;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminPost extends SearchCriteria {

    // posts 테이블 컬럼 (제공된 스키마 기반)
    private String postSn;         // pst_sn
    private String boardClassCode; // bbs_clsf_cd
    private String writerUserNo;   // wrtr_user_no
    private String postTitle;      // pst_ttl
    private String postContent;    // pst_cn
    private int viewCount;         // view_cnt
    private boolean noticePostYn;  // ntc_pst_yn
    private LocalDateTime createdDate;    // crt_dt
    private LocalDateTime modifiedDate;   // mdfcn_dt
    private LocalDateTime deletedDate;    // del_dt
    private String deleteUserNo;   // (게시글 숨김 처리 시 사용될 관리자 번호, posts 테이블에 추가 필요 가능성 있음)

    // JOIN 또는 서브쿼리를 통해 가져올 추가 정보
    private String userNickname;         // users.user_ncnm
    private String boardName;            // boards.bbs_nm
    private String representativeImg;    // post_images.img_file_path_nm
    private int likeCount;               // post_interactions COUNT
    private int commentCount;            // post_comments COUNT
    
    // 편의 필드 (AdminFeed.java와 동일)
    private String psize;

    public int getPsizeAsInt() {
        if (this.psize == null || this.psize.trim().isEmpty()) {
            return 10; // 기본값
        }
        try {
            return Integer.parseInt(this.psize);
        } catch (NumberFormatException e) {
            return 10; // 숫자로 변환할 수 없는 경우에도 기본값 반환
        }
    }
}