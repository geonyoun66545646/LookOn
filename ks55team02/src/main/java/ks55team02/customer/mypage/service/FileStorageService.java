package ks55team02.customer.mypage.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


public class FileStorageService {

	@Value("${profile.image.path}")
    private String uploadPath;

	public String storeProfileImage(MultipartFile file) throws IOException {
        // 1. 업로드 디렉토리 생성
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 2. 파일명 생성 (UUID + 원본파일명)
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String newFilename = UUID.randomUUID() + "_" + originalFilename;

        // 3. 파일 저장
        Path targetPath = uploadDir.resolve(newFilename);
        file.transferTo(targetPath);

        // 4. 웹 접근 경로 반환 (예: /profile-images/파일명)
        return "/profile-images/" + newFilename;
    }
}
