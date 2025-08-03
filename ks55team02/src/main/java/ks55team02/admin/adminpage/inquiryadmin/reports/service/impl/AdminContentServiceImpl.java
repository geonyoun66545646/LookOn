package ks55team02.admin.adminpage.inquiryadmin.reports.service.impl;

import org.springframework.stereotype.Service;

import ks55team02.admin.adminpage.inquiryadmin.reports.mapper.AdminContentMapper;
import ks55team02.admin.adminpage.inquiryadmin.reports.service.AdminContentService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminContentServiceImpl implements AdminContentService {

    private final AdminContentMapper adminContentMapper;

    @Override
    public Object getOriginalPost(String contentId) {
        return adminContentMapper.findOriginalPostById(contentId);
    }

    @Override
    public Object getOriginalComment(String contentId) {
        return adminContentMapper.findOriginalCommentById(contentId);
    }

    @Override
    public Object getOriginalProduct(String contentId) {
        return adminContentMapper.findOriginalProductById(contentId);
    }

    @Override
    public Object getOriginalUser(String userId) {
        return adminContentMapper.findOriginalUserById(userId);
    }
}