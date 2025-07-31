package ks55team02.admin.adminpage.boardadmin.feedmanagement.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.AdminFeed;
import ks55team02.admin.adminpage.boardadmin.feedmanagement.domain.FeedPreviewDto;

@Mapper
public interface FeedManagementMapper {

	List<AdminFeed> selectFeedList(AdminFeed searchCriteria);

	int selectFeedCount(AdminFeed searchCriteria);

	int updateFeedsToHidden(Map<String, Object> params);

	int updateFeedsToRestored(List<String> feedSns);
	
	FeedPreviewDto selectFeedPreviewById(@Param("feedSn") String feedSn);
}