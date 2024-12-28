package com.springcrud.springcrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.springcrud.Model"})
@ComponentScan(basePackages = {"com.springcrud.Controller"})
@EnableJpaRepositories(basePackages = "com.springcrud.Repo")
public class SpringcrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringcrudApplication.class, args);
	}

}
