package ks55team02.admin.adminpage.useradmin.search.domain;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminSearchLog {

    /**
     * 검색 로그 ID (PK)
     */
    private String srchLogId;

    /**
     * 검색한 사용자 번호 (FK)
     */
    private String userNo;

    /**
     * 검색 키워드
     */
    private String srchKeywordNm;

    /**
     * 검색 일시
     */
    private LocalDateTime srchDt;

    /**
     * 검색 시 사용자 IP 주소
     */
    private String srchUserIpAddr;

    /*
     * 멘토의 추가 의견:
     * 나중에 '로그 보기' 화면에서 사용자 아이디나 닉네임도 보여주고 싶을 수 있습니다.
     * 그럴 때를 대비해서 아래처럼 user 테이블과 조인한 결과를 담을 필드를 추가해두면
     * DTO를 수정하지 않고도 유연하게 확장할 수 있습니다. 지금은 주석 처리해두겠습니다.
     */
    // private String userId;
    // private String userNickname;
}