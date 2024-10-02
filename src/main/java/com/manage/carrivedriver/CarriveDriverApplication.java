package com.manage.carrivedriver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = {"com.manage.carriveutility.repository"})
@EntityScan("com.manage.carrive")
public class CarriveDriverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarriveDriverApplication.class, args);
    }

}
