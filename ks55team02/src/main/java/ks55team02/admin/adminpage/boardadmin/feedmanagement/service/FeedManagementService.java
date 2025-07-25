package ks55team02.admin.adminpage.boardadmin.feedmanagement.service;

import java.util.List;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.FeedPreviewDto;

public interface FeedManagementService {

    /**
     * 조건에 맞는 피드 목록을 조회합니다.
     * @param searchCriteria 검색 조건
     * @return 피드 목록
     */
    List<AdminFeed> selectFeedList(AdminFeed searchCriteria);

    /**
     * 조건에 맞는 전체 피드의 개수를 조회합니다.
     * @param searchCriteria 검색 조건
     * @return 전체 피드 개수
     */
    int selectFeedCount(AdminFeed searchCriteria);

    /**
     * 여러 피드의 상태를 일괄적으로 변경합니다. (숨김/복구 처리)
     * @param params 'feedSns' (피드 번호 목록)와 'delUserNo' (처리자 번호) 포함
     * @return 처리된 행의 수
     */
    int updateFeedsToHidden(List<String> feedSns, String adminUserNo);
    
    /**
     * 여러 피드를 복구합니다.
     * @param feedSns 복구할 피드 번호 목록
     * @return 처리된 행의 수
     */
    int updateFeedsToRestored(List<String> feedSns);
    
    FeedPreviewDto getFeedPreview(String feedSn);
}