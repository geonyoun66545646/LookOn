package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class AdminSanctionDetail {
    private String sanctionType;
    private LocalDateTime sanctionStartDt;
    private LocalDateTime sanctionEndDt;
}