package ks55team02.admin.adminpage.boardadmin.postmanagement.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.boardadmin.postmanagement.domain.AdminPost;
import ks55team02.admin.adminpage.boardadmin.postmanagement.domain.PostPreviewDto;
import ks55team02.admin.adminpage.boardadmin.postmanagement.mapper.PostManagementMapper;
import ks55team02.admin.adminpage.boardadmin.postmanagement.service.PostManagementService;
import ks55team02.customer.post.domain.Post; // [신규] 고객 DTO import
import ks55team02.customer.post.service.PostService; // [신규] 고객 Service import
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class PostManagementServiceImpl implements PostManagementService {

    private final PostManagementMapper postManagementMapper;
    // [신규] 고객 측 PostService 의존성 주입
    private final PostService customerPostService; 

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

    // [핵심 수정] 기존의 PostService를 재사용하여 게시글을 등록합니다.
    @Override
    public void insertPost(AdminPost adminPost) {
        Post post = new Post();
        post.setBbsClsfCd(adminPost.getBoardClassCode());
        post.setWrtrUserNo(adminPost.getWriterUserNo());
        post.setPstTtl(adminPost.getPostTitle());
        post.setPstCn(adminPost.getPostContent());

        customerPostService.insertPost(post, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PostPreviewDto getPostPreview(String postSn) {
        return postManagementMapper.selectPostPreviewById(postSn);
    }
}