package ks55team02.customer.reports.service.impl;

// --- [ 1. 수정된 Import 목록 ] ---
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ks55team02.customer.reports.domain.ReportAttachment;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;
import ks55team02.customer.reports.mapper.ReportsMapper;
import ks55team02.customer.reports.service.ReportsService;

@Service
@Transactional
public class ReportsServiceImpl implements ReportsService {

    // --- [ 2. 추가된 멤버 변수 ] ---
    private static final Logger log = LoggerFactory.getLogger(ReportsServiceImpl.class);
    @Value("${file.path}")
    private String fileRealPath;

    private final ReportsMapper reportsMapper;

    public ReportsServiceImpl(ReportsMapper reportsMapper) {
        this.reportsMapper = reportsMapper;
    }

    @Override
    public List<String> getReportTargetTypeList() {
        return reportsMapper.getReportTargetTypeList();
    }

    @Override
    public List<ReportsReasons> getActiveReportReasonList(String targetType) {
        return reportsMapper.getActiveReportReasonList(targetType);
    }

    // --- [ 3. 로직이 추가된 addReport 메소드 ] ---
    @Override
    public void addReport(Reports report, List<MultipartFile> evidenceFiles) throws IOException {

        String latestId = reportsMapper.getLatestReportId();
        int newIdNum = 1;
        if (latestId != null && latestId.startsWith("RPT_ID_")) {
            String numericPart = latestId.substring(7);
            newIdNum = Integer.parseInt(numericPart) + 1;
        }
        String newReportId = String.format("RPT_ID_%03d", newIdNum);
        report.setDclrId(newReportId);

        reportsMapper.addReport(report);

        if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
            for (MultipartFile file : evidenceFiles) {
                if (file.isEmpty()) continue;

                String saveDirectory = fileRealPath + "/attachment/reports";
                String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String dateDirectory = saveDirectory + "/" + today;

                File dir = new File(dateDirectory);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = file.getOriginalFilename();
                String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

                Path filePath = Paths.get(dateDirectory, uniqueFilename);
                file.transferTo(filePath);

                ReportAttachment attachment = new ReportAttachment();
                attachment.setFileId(UUID.randomUUID().toString());
                attachment.setDclrId(newReportId);
                attachment.setOriginalFilename(originalFilename);
                attachment.setFilePath("/attachment/reports/" + today + "/" + uniqueFilename);
                attachment.setFileSize(file.getSize());

                // ※ 아직 ReportsMapper에 이 메소드가 없어서 여기서 오류가 날 겁니다.
                //   다음 단계에서 바로 추가할 예정입니다.
                reportsMapper.insertReportAttachment(attachment);
            }
        }
    }
}