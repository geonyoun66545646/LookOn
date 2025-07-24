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
    public List<AdminPost> selectPostList(AdminPost searchCriteria) {
        return postManagementMapper.selectPostList(searchCriteria);
    }

    @Override
    @Transactional(readOnly = true)
    public int selectPostCount(AdminPost searchCriteria) {
        return postManagementMapper.selectPostCount(searchCriteria);
    }

    @Override
    public int updatePostsToHidden(List<String> postSns) {
        if (postSns == null || postSns.isEmpty()) {
            return 0;
        }
        return postManagementMapper.updatePostsToHidden(postSns);
    }

    @Override
    public int updatePostsToRestored(List<String> postSns) {
        if (postSns == null || postSns.isEmpty()) {
            return 0;
        }
        return postManagementMapper.updatePostsToRestored(postSns);
    }
}