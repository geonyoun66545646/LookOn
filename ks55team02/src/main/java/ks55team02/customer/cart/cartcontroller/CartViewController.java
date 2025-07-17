package ks55team02.customer.cart.cartcontroller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartViewController {
    /**
     * 장바구니 뷰 페이지를 반환합니다.
     * GET /cart
     * @return 장바구니 HTML 뷰 경로
     */
    @GetMapping(value = {"/cart"})
    public String customerCartView() {
        // cart.html 파일이 src/main/resources/templates/customer/fragments/cart.html 에 있다고 가정합니다.
        return "customer/fragments/cart";
    }
}
