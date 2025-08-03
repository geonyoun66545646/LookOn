package ks55team02.admin.adminpage.inquiryadmin.reports.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.Map;

@Mapper
public interface AdminContentMapper {
    Map<String, Object> findOriginalPostById(String contentId);
    Map<String, Object> findOriginalCommentById(String contentId);
    Map<String, Object> findOriginalProductById(String contentId);
    Map<String, Object> findOriginalUserById(String userId);
}