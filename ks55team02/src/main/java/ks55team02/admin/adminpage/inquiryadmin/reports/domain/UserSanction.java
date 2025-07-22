package ks55team02.admin.adminpage.inquiryadmin.reports.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserSanction {
    private String sanctionId;
    private String userNo;
    private String dclrId;
    private String sanctionType;
    private String sanctionReason;
    private LocalDateTime sanctionStartDt;
    private LocalDateTime sanctionEndDt;
    private LocalDateTime prcsDt;
    private String prcsAdminNo;
}