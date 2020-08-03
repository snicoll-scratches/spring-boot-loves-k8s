package com.example.k8s.hello.app;

import com.example.k8s.hello.service.HelloService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {

	private final HelloService helloService;

	HelloController(HelloService helloService) {
		this.helloService = helloService;
	}

	@GetMapping("/hello")
	String hello(@RequestParam String name) {
		return this.helloService.hello(name);
	}

}
