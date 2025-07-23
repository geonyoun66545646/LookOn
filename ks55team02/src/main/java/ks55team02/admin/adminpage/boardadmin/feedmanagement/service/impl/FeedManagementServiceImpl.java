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
    public List<AdminFeed> getFeedList(AdminFeed searchCriteria) {
        return feedManagementMapper.getFeedList(searchCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public int getFeedCount(AdminFeed searchCriteria) {
        return feedManagementMapper.getFeedCount(searchCriteria);
    }

    @Override
    public int hideFeeds(List<String> feedSns, String adminUserNo) {
        if (feedSns == null || feedSns.isEmpty()) {
            return 0;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("feedSns", feedSns);
        params.put("adminUserNo", adminUserNo);
        return feedManagementMapper.hideFeeds(params);
    }

    @Override
    public int restoreFeeds(List<String> feedSns) {
        if (feedSns == null || feedSns.isEmpty()) {
            return 0;
        }
        return feedManagementMapper.restoreFeeds(feedSns);
    }
}