package ks55team02.seller.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/seller") // URL을 /seller로 변경
public class ProductSellerHomeController {

    @GetMapping(value={"", "/"})
    public String sellerMainView() { // 메서드 이름도 변경 가능

        return "seller/main"; // 템플릿 경로는 그대로 유지
    }
}