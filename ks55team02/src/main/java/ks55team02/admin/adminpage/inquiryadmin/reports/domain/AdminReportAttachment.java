package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReportAttachment {
    private String filePath; // 웹에서 접근 가능한 URL 경로
    private String originalFileName; // 원본 파일명
}