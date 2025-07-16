package ks55team02.admin.adminpage.useradmin.search.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AdminSearchStats {

    /**
     * 검색된 키워드 이름 (GROUP BY의 대상)
     */
    private String srchKeywordNm;

    /**
     * 해당 키워드가 검색된 횟수 (COUNT(*)의 결과)
     */
    private int searchCount;

    /*
     * 멘토의 추가 의견:
     * 통계 순위를 보여주고 싶을 경우를 대비해 순위 필드를 추가할 수도 있습니다.
     * SQL에서 RANK() OVER (ORDER BY COUNT(*) DESC) 같은 윈도우 함수를 사용하면
     * 쉽게 순위를 매길 수 있고, 그 결과를 이 필드에 담으면 됩니다.
     */
    // private int ranking;
}