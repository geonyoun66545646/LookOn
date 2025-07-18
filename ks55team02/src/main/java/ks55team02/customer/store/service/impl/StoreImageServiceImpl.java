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
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StoreImageServiceImpl implements StoreImageService {

    private final FilesUtils filesUtils;
    private final StoreImageMapper storeImageMapper;

    @Override
    public String addStoreImage(MultipartFile file) { // String 반환 타입으로 변경
        FileDetail fileDetail = filesUtils.saveFile(file, "store_images");
        
        if (fileDetail != null) {
            // 파일 저장 성공 시 이미지 ID 생성 및 DB 저장
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
            storeImageMapper.addStoreImage(storeImage); // DB에 이미지 정보 삽입
            return imgId; // 생성된 imgId 반환
        }
        return null; // 파일 저장 실패 시 null 반환
    }

    @Override
    public void addStoreImages(MultipartFile[] files) {
        // 이 메서드는 현재 addStoreApplication에서 사용되지 않지만, 완전성을 위해 유지
        List<FileDetail> fileDetailsList = filesUtils.saveFiles(files, "store_images");
        
        if (!fileDetailsList.isEmpty()) {
            List<StoreImage> storeImageList = new ArrayList<>();
            for (FileDetail fileDetail : fileDetailsList) {
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
        String fullPath = storeImage.getImgAddr();
        Boolean isDeleted = filesUtils.deleteFileByPath(fullPath);

        if (isDeleted) {
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
