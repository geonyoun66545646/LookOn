// ks55team02.admin.adminpage.storeadmin.appadmin.controller.AppAdminFileController.java
package ks55team02.admin.adminpage.storeadmin.appadmin.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import ks55team02.admin.adminpage.storeadmin.appadmin.service.AppAdminFileService;
import ks55team02.common.domain.store.StoreImage;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/adminpage/storeadmin/files") // URL 경로를 명확하게 지정
@RequiredArgsConstructor
public class AppAdminFileController {

    private final AppAdminFileService appAdminFileService;

    // "자세히 보기" 기능: 파일을 브라우저에서 바로 열기
    @GetMapping("/view/{imgId}")
    @ResponseBody
    public ResponseEntity<Resource> viewFile(@PathVariable String imgId) {
        StoreImage imageInfo = appAdminFileService.getImageInfo(imgId);
        if (imageInfo == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = appAdminFileService.loadFileAsResource(imageInfo);

        return ResponseEntity.ok()
                .contentType(getMediaTypeForFileType(imageInfo.getImgTypeCd()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageInfo.getImgFileNm() + "\"")
                .body(resource);
    }

    // "다운로드" 기능: 파일을 PC에 저장
    @GetMapping("/download/{imgId}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String imgId) {
        StoreImage imageInfo = appAdminFileService.getImageInfo(imgId);
        if (imageInfo == null) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = appAdminFileService.loadFileAsResource(imageInfo);

        // 파일명에 한글, 공백 등이 있을 경우를 대비해 URL 인코딩 처리
        String encodedFileName = URLEncoder.encode(imageInfo.getImgFileNm(), StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(resource);
    }

    /**
     * 파일 타입 코드(JPG, PNG 등)를 Spring의 MediaType으로 변환하는 헬퍼 메서드
     * @param fileType DB에 저장된 이미지 유형 코드
     * @return MediaType
     */
    private MediaType getMediaTypeForFileType(String fileType) {
        if (fileType == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        String upperCaseFileType = fileType.toUpperCase();
        switch (upperCaseFileType) {
            case "JPG":
            case "JPEG":
                return MediaType.IMAGE_JPEG;
            case "PNG":
                return MediaType.IMAGE_PNG;
            case "GIF":
                return MediaType.IMAGE_GIF;
            case "PDF":
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}