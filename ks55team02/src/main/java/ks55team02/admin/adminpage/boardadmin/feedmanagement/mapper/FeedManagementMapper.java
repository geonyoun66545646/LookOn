package ks55team02.admin.adminpage.boardadmin.feedmanagement.mapper;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface FeedManagementMapper {

    List<AdminFeed> getFeedList(AdminFeed searchCriteria);

    int getFeedCount(AdminFeed searchCriteria);

    int hideFeeds(Map<String, Object> params);
    
    int restoreFeeds(List<String> feedSns);
}