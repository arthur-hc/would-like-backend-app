package com.wouldlike.backendapp;

import org.springframework.boot.SpringApplication;

public class TestWouldLikeBackendAppApplication {

	public static void main(String[] args) {
		SpringApplication.from(WouldLikeBackendAppApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
