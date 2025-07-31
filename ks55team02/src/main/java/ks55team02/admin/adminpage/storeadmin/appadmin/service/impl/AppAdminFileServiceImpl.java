package ks55team02.admin.adminpage.storeadmin.appadmin.service.impl;

import ks55team02.admin.adminpage.storeadmin.appadmin.mapper.AppAdminFileMapper;
import ks55team02.admin.adminpage.storeadmin.appadmin.service.AppAdminFileService;
import ks55team02.common.domain.store.StoreImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 로거 추가
import org.springframework.beans.factory.annotation.Value; // @Value import
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j // 로깅을 위해 Slf4j 어노테이션 추가
public class AppAdminFileServiceImpl implements AppAdminFileService {

    private final AppAdminFileMapper appAdminFileMapper;

    // FilesUtils와 동일한 프로퍼티 값을 주입받습니다.
    @Value("${file.path}")
    private String fileRootPath;

    @Override
    public StoreImage getImageInfo(String imgId) {
        return appAdminFileMapper.findImageById(imgId);
    }

    @Override
    public Resource loadFileAsResource(StoreImage imageInfo) {
        try {
            // DB에 저장된 상대 경로 (예: /attachment/inquiry_images/...)
            String relativePathFromDb = imageInfo.getImgAddr();

            // 상대 경로의 시작이 '/' 또는 '\' 라면 제거합니다.
            // 이는 Paths.get().resolve() 메서드가 첫 번째 인자를 루트로 인식하는 것을 방지합니다.
            if (relativePathFromDb.startsWith("/") || relativePathFromDb.startsWith("\\")) {
                relativePathFromDb = relativePathFromDb.substring(1);
            }

            // file.path 값과 DB의 상대 경로를 조합하여 완전한 파일 시스템 경로를 생성합니다.
            // 예: "/home/teamproject" + "attachment/inquiry_images/..."
            Path filePath = Paths.get(fileRootPath).resolve(relativePathFromDb).normalize();

            // 디버깅을 위해 최종 경로를 로그로 출력합니다.
            log.info("파일 로드 시도 경로: {}", filePath);

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                // 파일이 없을 때 더 명확한 에러 로그를 남깁니다.
                log.error("파일을 찾을 수 없거나 읽을 수 없습니다. 확인된 경로: {}", filePath);
                throw new RuntimeException("파일을 찾을 수 없거나 읽을 수 없습니다: " + imageInfo.getImgFileNm());
            }
        } catch (MalformedURLException ex) {
            log.error("파일 경로가 올바르지 않습니다: {}", imageInfo.getImgFileNm(), ex);
            throw new RuntimeException("파일 경로가 올바르지 않습니다: " + imageInfo.getImgFileNm(), ex);
        }
    }
}