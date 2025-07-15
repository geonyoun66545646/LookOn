package ks55team02.customer.store.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ks55team02.common.domain.store.StoreImage;
import ks55team02.customer.store.service.StoreImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/store/images")
public class StoreImageController {

    @Value("${file.path}")
    private String fileRealPath;

    private final StoreImageService storeImageService; // StoreImageService 주입

    @GetMapping("/upload")
    public String uploadStoreImage(Model model) {
        model.addAttribute("title", "상점 이미지 업로드");
        return "admin/store/imageUpload";
    }

    @PostMapping("/upload")
    public String uploadStoreImagePro(@RequestPart(name = "files", required = false) MultipartFile[] files) {
        if (files != null && files.length > 0) {
            storeImageService.addStoreImages(files);
        }
        return "redirect:/admin/store/images/list";
    }

    @GetMapping("/list")
    public String listStoreImages(Model model) {
        model.addAttribute("title", "상점 이미지 목록");
        model.addAttribute("imageList", storeImageService.getStoreImageList());
        return "admin/store/imageList";
    }

    @GetMapping("/delete")
    public String deleteStoreImage(@RequestParam(value = "imgId") String imgId) {
        if (imgId != null) {
            StoreImage storeImage = storeImageService.getStoreImageById(imgId);
            if (storeImage != null) {
                storeImageService.deleteStoreImage(storeImage);
            }
        }
        return "redirect:/admin/store/images/list";
    }

    @RequestMapping(value = "/download")
    @ResponseBody
    public ResponseEntity<Object> downloadStoreImage(@RequestParam(value = "imgId", required = false) String imgId,
                                                   HttpServletRequest request,
                                                   HttpServletResponse response) throws URISyntaxException {
        if (imgId != null) {
            StoreImage storeImage = storeImageService.getStoreImageById(imgId);

            if (storeImage != null) {
                File file = new File(fileRealPath + storeImage.getImgAddr());

                Path path = Paths.get(file.getAbsolutePath());
                Resource resource;
                try {
                    resource = new UrlResource(path.toUri());
                    String contentType = null;
                    contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(storeImage.getImgFileNm(), "UTF-8") + "\";")
                            .body(resource);
                } catch (MalformedURLException e) {
                    log.error("URL 형식이 잘못되었습니다.", e);
                    e.printStackTrace();
                } catch (IOException e) {
                    log.error("파일 다운로드 중 오류 발생", e);
                    e.printStackTrace();
                }
            }
        }

        URI redirectUri = new URI("/");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(redirectUri);

        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }
}
