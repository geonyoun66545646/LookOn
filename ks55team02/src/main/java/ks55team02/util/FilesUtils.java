package ks55team02.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FilesUtils {

	@Value("${file.path}")
	private String fileRealPath; // 예: /home/teamproject/

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	public static final DateTimeFormatter FILEIDX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	
	/**
	 * 단일 MultipartFile을 저장하고, 저장된 파일의 상세 정보를 담은 FileDetail 객체를 반환합니다.
	 * 파일은 ${file.path}/attachment/{subDirectory}/{dateDirectory}/{typeDirectory}/ 에 저장됩니다.
	 *
	 * @param multipartFile 저장할 MultipartFile 객체
	 * @param subDirectory 파일이 저장될 "attachment/" 하위의 서브 디렉토리 (예: "store_images", "inquiry_images")
	 * @return 저장된 파일의 상세 정보 (FileDetail 객체), 파일이 비어있으면 null 반환
	 */
	public FileDetail saveFile(MultipartFile multipartFile, String subDirectory) {
		if (multipartFile.isEmpty()) {
			return null;
		}

		// 현재 날짜 기준으로 디렉토리 이름 생성
		LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
		String dateDirectory = now.format(DATE_FORMATTER);

		// 파일 분류 (컨텐츠 타입에 따라 'image' 또는 'files'로 분류)
		String contentType = multipartFile.getContentType();
		String typeDirectory = (contentType != null && contentType.contains("image")) ? "image" : "files";

		// 최종 저장 경로 설정: fileRealPath / attachment / subDirectory / dateDirectory / typeDirectory
		// 예: /home/teamproject/attachment/store_images/20231026/image/
		Path folderPath = Paths.get(fileRealPath, "attachment", subDirectory, dateDirectory, typeDirectory);
		
		// 디렉토리 생성
		createFolder(folderPath);

		String originalFileName = multipartFile.getOriginalFilename();
		String extension = getExtension(originalFileName); // 점(.) 없는 확장자
		String newFileName = UUID.randomUUID() + "." + extension; // 확장자 앞에 점(.) 추가
		
		// OS 환경에 맞춰 경로 설정
		Path uploadPath = folderPath.resolve(newFileName);

		try {
			// 파일 저장
			Files.write(uploadPath, multipartFile.getBytes());

			// fileRealPath를 제외한 상대 경로 계산
			// 예: /attachment/store_images/20231026/image/uuid.jpg
			String savedRelativePath = fileRealPath.replace("\\", "/"); // fileRealPath를 먼저 정규화
			savedRelativePath = uploadPath.toString().replace("\\", "/").replace(savedRelativePath, "");

			// FileDetail 객체 생성 및 반환
			return FileDetail.builder()
					.originalFileName(originalFileName)
					.newFileName(newFileName)
					.savedPath(savedRelativePath)
					.fileSize(multipartFile.getSize())
					.fileExtension(extension.toUpperCase()) // 확장자를 대문자로 저장
					.fileType(typeDirectory) // 파일 유형 정보
					.build();

		} catch (IOException e) {
			e.printStackTrace();
			// 파일 저장 실패 시 예외 처리
			throw new RuntimeException("파일 저장 실패: " + originalFileName, e);
		}
	}

	/**
	 * 여러 MultipartFile을 저장하고, 각 파일의 상세 정보를 담은 FileDetail 객체 리스트를 반환합니다.
	 *
	 * @param multipartFiles 저장할 MultipartFile 배열
	 * @param subDirectory 파일이 저장될 "attachment/" 하위의 서브 디렉토리
	 * @return 저장된 파일들의 상세 정보 리스트
	 */
	public List<FileDetail> saveFiles(MultipartFile[] multipartFiles, String subDirectory) {
		List<FileDetail> fileDetailsList = new ArrayList<>();
		if (multipartFiles != null) {
			for (MultipartFile multipartFile : multipartFiles) {
				FileDetail fileDetail = saveFile(multipartFile, subDirectory);
				if (fileDetail != null) {
					fileDetailsList.add(fileDetail);
				}
			}
		}
		return fileDetailsList;
	}

	/**
	 * 지정된 경로의 파일을 삭제합니다.
	 *
	 * @param filePath 삭제할 파일의 상대 경로 (fileRealPath를 제외한 부분)
	 * 예: /attachment/store_images/20231026/image/uuid.jpg
	 * @return 파일 삭제 성공 여부
	 */
	public boolean deleteFileByPath(String filePath) {
		// fileRealPath를 다시 붙여서 전체 절대 경로를 구성
		Path deletePath = Paths.get(fileRealPath, filePath);
		try {
			System.out.println("삭제 시도 경로: " + deletePath);
			return Files.deleteIfExists(deletePath); // 파일이 존재하면 삭제하고 true 반환, 없으면 false 반환
		} catch (Exception e) {
			e.printStackTrace();
			// 파일 삭제 실패 시 예외 처리
			throw new RuntimeException("파일 삭제 실패: " + filePath, e);
		}
	}

	// --- 내부 헬퍼 메서드 ---

	// 폴더 생성
	private void createFolder(Path path) {
		try {
			Files.createDirectories(path);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("디렉토리 생성 실패: " + path, e);
		}
	}
	
	// 확장자 반환 (점(.)을 제외한 순수 확장자만 반환)
	private String getExtension(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return "";
		}
		int dotIdx = fileName.lastIndexOf(".");
		return (dotIdx > 0 && dotIdx < fileName.length() - 1)
				? fileName.substring(dotIdx + 1)
				: "";		
	}
}
