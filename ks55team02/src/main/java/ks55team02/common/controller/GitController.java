package ks55team02.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GitController {

	@GetMapping("/git")
	public String GitControllers(){
		
	return "projectinfomation";
	}
}
