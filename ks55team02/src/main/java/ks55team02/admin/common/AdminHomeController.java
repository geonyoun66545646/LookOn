package ks55team02.admin.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="/admin")
public class AdminHomeController {
	
	@GetMapping(value= {"/main","/main/"})
    public String adminHomeView() {

        return "/admin/main";
    }	
	

}
