package com.madao.disk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan(basePackages = "com.madao.disk.mapper")
@SpringBootApplication
public class DiskApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiskApplication.class, args);
	}

}
