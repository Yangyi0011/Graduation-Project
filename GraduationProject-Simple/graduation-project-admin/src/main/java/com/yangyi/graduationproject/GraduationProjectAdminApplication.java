package com.yangyi.graduationproject;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer  //指定为BootAdmin-Sever端
@SpringBootApplication
public class GraduationProjectAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraduationProjectAdminApplication.class, args);
	}

}
