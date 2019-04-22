package com.yangyi.graduationproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//扫描 mybatis dao层的接口
@MapperScan(value = "com.yangyi.graduationproject.dao")
@SpringBootApplication
public class GraduationProjectMvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraduationProjectMvcApplication.class, args);
	}

}
