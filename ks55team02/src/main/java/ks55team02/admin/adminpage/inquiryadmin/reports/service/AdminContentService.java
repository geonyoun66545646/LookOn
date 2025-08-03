package ks55team02.admin.adminpage.inquiryadmin.reports.service;

public interface AdminContentService {
    Object getOriginalPost(String contentId);
    Object getOriginalComment(String contentId);
    Object getOriginalProduct(String contentId);
    Object getOriginalUser(String userId);
}