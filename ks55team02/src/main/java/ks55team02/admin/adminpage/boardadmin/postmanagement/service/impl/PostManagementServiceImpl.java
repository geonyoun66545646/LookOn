package ks55team02.admin.adminpage.boardadmin.postmanagement.service.impl;

import ks55team02.admin.adminpage.boardadmin.postmanagement.domain.AdminPost;
import ks55team02.admin.adminpage.boardadmin.postmanagement.mapper.PostManagementMapper;
import ks55team02.admin.adminpage.boardadmin.postmanagement.service.PostManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostManagementServiceImpl implements PostManagementService {

    private final PostManagementMapper postManagementMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AdminPost> getPostList(AdminPost searchCriteria) {
        return postManagementMapper.getPostList(searchCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public int getPostCount(AdminPost searchCriteria) {
        return postManagementMapper.getPostCount(searchCriteria);
    }

    @Override
    public int hidePosts(List<String> postSns) {
        if (postSns == null || postSns.isEmpty()) {
            return 0;
        }
        return postManagementMapper.hidePosts(postSns);
    }

    @Override
    public int restorePosts(List<String> postSns) {
        if (postSns == null || postSns.isEmpty()) {
            return 0;
        }
        return postManagementMapper.restorePosts(postSns);
    }
}