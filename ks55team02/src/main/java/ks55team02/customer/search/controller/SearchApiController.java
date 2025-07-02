package ks55team02.customer.search.controller;

import ks55team02.customer.search.domain.SearchPagination; // SearchPagination import 추가
import ks55team02.customer.search.service.SearchService;
import ks55team02.customer.search.domain.ProductsSearch;
import ks55team02.customer.search.domain.PostsSearch;
import ks55team02.customer.search.domain.UsersSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchApiController {

    private final SearchService searchService;

    /**
     * 1. 상품 검색 결과를 JSON으로 반환하는 API (페이지네이션 적용)
     * @param keyword 검색어
     * @param page 요청 페이지 번호 (기본값 1)
     * @return 페이지네이션 정보가 포함된 상품 목록
     */
    @GetMapping("/products")
    public ResponseEntity<SearchPagination<ProductsSearch>> searchProducts(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        
        log.info("API 호출 - 상품 검색: keyword={}, page={}", keyword, page);
        SearchPagination<ProductsSearch> productPage = searchService.getProductsList(keyword, page);
        return new ResponseEntity<>(productPage, HttpStatus.OK);
    }

    /**
     * 2. 게시글 검색 결과를 JSON으로 반환하는 API (페이지네이션 적용)
     */
    @GetMapping("/posts")
    public ResponseEntity<SearchPagination<PostsSearch>> searchPosts(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        
        log.info("API 호출 - 게시글 검색: keyword={}, page={}", keyword, page);
        SearchPagination<PostsSearch> postPage = searchService.getPostsList(keyword, page);
        return new ResponseEntity<>(postPage, HttpStatus.OK);
    }

    /**
     * 3. 사용자 검색 결과를 JSON으로 반환하는 API (페이지네이션 적용)
     */
    @GetMapping("/users")
    public ResponseEntity<SearchPagination<UsersSearch>> searchUsers(
            @RequestParam(value = "keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        
        log.info("API 호출 - 사용자 검색: keyword={}, page={}", keyword, page);
        SearchPagination<UsersSearch> userPage = searchService.getUsersList(keyword, page);
        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }
}