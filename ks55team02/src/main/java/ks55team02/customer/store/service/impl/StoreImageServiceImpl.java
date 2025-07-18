package ks55team02.customer.store.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.common.domain.store.StoreImage;
import ks55team02.customer.store.mapper.StoreImageMapper;
import ks55team02.customer.store.service.StoreImageService;
import ks55team02.util.FileDetail; // 변경된 FilesUtils 패키지 및 FileDetail import
import ks55team02.util.FilesUtils; // 변경된 FilesUtils 패키지 import
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StoreImageServiceImpl implements StoreImageService {

    private final FilesUtils filesUtils; // 파일 저장 및 삭제 유틸리티
    private final StoreImageMapper storeImageMapper; // StoreImage 데이터베이스 매퍼

    @Override
    public void addStoreImage(MultipartFile file) {
        // FilesUtils를 사용하여 파일을 저장하고 FileDetail 객체를 반환받습니다.
        // "store_images"는 이 파일이 저장될 서브 디렉토리입니다.
        FileDetail fileDetail = filesUtils.saveFile(file, "store_images");
        
        if (fileDetail != null) {
            // FileDetail 정보를 사용하여 StoreImage 객체를 구성합니다.
            // imgId는 StoreImage의 고유 ID로, FileDetail의 정보와는 별개로 생성합니다.
            String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                           + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

            StoreImage storeImage = StoreImage.builder()
                    .imgId(imgId)
                    .imgFileNm(fileDetail.getOriginalFileName())
                    .imgAddr(fileDetail.getSavedPath()) // FileDetail에서 저장된 상대 경로 사용
                    .imgFileSz(fileDetail.getFileSize())
                    .imgTypeCd(fileDetail.getFileExtension()) // FileDetail에서 확장자 가져오기
                    .regYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                    .delYn(false)
                    .build();
            storeImageMapper.addStoreImage(storeImage);
        }
    }

    @Override
    public void addStoreImages(MultipartFile[] files) {
        // FilesUtils를 사용하여 여러 파일을 저장하고 FileDetail 객체 리스트를 반환받습니다.
        List<FileDetail> fileDetailsList = filesUtils.saveFiles(files, "store_images");
        
        if (!fileDetailsList.isEmpty()) {
            List<StoreImage> storeImageList = new ArrayList<>();
            for (FileDetail fileDetail : fileDetailsList) {
                // 각 FileDetail 정보를 사용하여 StoreImage 객체를 구성합니다.
                String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                               + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

                StoreImage storeImage = StoreImage.builder()
                        .imgId(imgId)
                        .imgFileNm(fileDetail.getOriginalFileName())
                        .imgAddr(fileDetail.getSavedPath())
                        .imgFileSz(fileDetail.getFileSize())
                        .imgTypeCd(fileDetail.getFileExtension())
                        .regYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .delYn(false)
                        .build();
                storeImageList.add(storeImage);
            }
            storeImageMapper.addStoreImages(storeImageList);
        }
    }

    @Override
    public void deleteStoreImage(StoreImage storeImage) {
        // 실제 파일 삭제 (FilesUtils 사용)
        String fullPath = storeImage.getImgAddr();
        Boolean isDeleted = filesUtils.deleteFile(fullPath);

        if (isDeleted) {
            // 데이터베이스에서 del_yn을 1로 업데이트 (실제 삭제 대신 논리적 삭제)
            storeImageMapper.deleteStoreImageById(storeImage.getImgId());
        } else {
            log.warn("파일 삭제 실패: {}", fullPath);
        }
    }

    @Override
    public StoreImage getStoreImageById(String imgId) {
        return storeImageMapper.getStoreImageById(imgId);
    }

    @Override
    public List<StoreImage> getStoreImageList() {
        return storeImageMapper.getStoreImageList();
    }
}
