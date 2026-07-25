package com.q4magic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = { "com.q4magic.*" })
@EntityScan(basePackages = { "com.q4magic" })
@EnableJpaRepositories(basePackages = { "com.q4magic.common.repository" })
public class Q4magicApplication {

	public static void main(String[] args) {
		SpringApplication.run(Q4magicApplication.class, args);
        System.out.println("======================================== Server started ==================================");
	}

}
