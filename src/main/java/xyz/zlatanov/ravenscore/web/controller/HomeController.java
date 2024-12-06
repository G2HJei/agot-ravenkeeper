package xyz.zlatanov.ravenscore.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	String home() {
		return "home";
	}
}
