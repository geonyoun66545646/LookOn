package ks55team02.customer.search.controller;

import ks55team02.customer.search.domain.Search;
import ks55team02.customer.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList; // ArrayList를 사용하기 위해 import

@Controller
@RequestMapping("/customer")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/searchResult")
    public String showSearchResults(
            // ★★★ @RequestParam을 아래와 같이 수정 ★★★
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword, 
            Model model) {

        Search searchData;

        // keyword가 비어있거나 공백만 있는지 확인
        if (keyword == null || keyword.trim().isEmpty()) {
            // 키워드가 비어있으면, 비어있는 리스트를 가진 빈 검색 결과 객체를 생성
            searchData = new Search(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        } else {
            // 키워드가 있으면 정상적으로 검색 실행
            searchData = searchService.searchAll(keyword);
        }

        model.addAttribute("searchResult", searchData);
        model.addAttribute("keyword", keyword);
        

        return "customer/search/searchResult";
    }
}