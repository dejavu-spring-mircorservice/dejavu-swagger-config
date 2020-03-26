package com.dejavu.swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableEurekaClient
@EnableScheduling
public class DejavuSwaggerConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(DejavuSwaggerConfigApplication.class, args);
	}

}
