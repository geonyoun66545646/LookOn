package ks55team02.util;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 파일 시스템에 저장된 파일의 상세 정보를 담는 범용 DTO.
 * 이 정보는 각 도메인(예: StoreImage, ProductImage)의 DTO를 구성하는 데 사용됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDetail {
    private String originalFileName; // 원본 파일명
    private String newFileName;      // 시스템에 저장된 새 파일명 (UUID 포함)
    private String savedPath;        // 파일이 저장된 상대 경로 (file.path를 제외한 부분)
    private Long fileSize;           // 파일 크기 (bytes)
    private String fileExtension;    // 파일 확장자 (점(.) 제외)
    private String fileType;         // 파일 유형 (예: "image", "files")
}
