package com.example.k8s.hello.service;

public class HelloService {

	public String hello(String name) {
		return String.format("Hello %s", name);
	}

}
