package com.springbootrest.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.web.bind.annotation.RestController
public class RestController {

	@GetMapping("/showRestGreeting")
	public String showGreeting() {
		return "hello";
	}
	
	@GetMapping("/showRestGoodBye")
	public String sayBye() {
		return "bye";
	}
}
