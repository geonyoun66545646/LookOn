package ks55team02.admin.adminpage.boardadmin.feedmanagement.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.mapper.FeedManagementMapper;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.service.FeedManagementService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedManagementServiceImpl implements FeedManagementService {

    private final FeedManagementMapper feedManagementMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminFeed> selectFeedList(AdminFeed searchCriteria) {
        return feedManagementMapper.selectFeedList(searchCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public int selectFeedCount(AdminFeed searchCriteria) {
        return feedManagementMapper.selectFeedCount(searchCriteria);
    }

    @Override
    public int updateFeedsToHidden(List<String> feedSns, String adminUserNo) {
        if (feedSns == null || feedSns.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("feedSns", feedSns);
        params.put("adminUserNo", adminUserNo);
        return feedManagementMapper.updateFeedsToHidden(params);
    }

    @Override
    public int updateFeedsToRestored(List<String> feedSns) {
        if (feedSns == null || feedSns.isEmpty()) {
            return 0;
        }
        return feedManagementMapper.updateFeedsToRestored(feedSns);
    }
}