package ks55team02.customer.reports.domain;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportAttachment {
	private String fileId;
	private String dclrId;
	private String originalFilename;
	private String filePath;
	private long fileSize;
	private LocalDateTime uploadDt;
}