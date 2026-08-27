package com.helix.gpo.web_crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

@Modulithic(sharedModules = "shared")
@EnableScheduling
@SpringBootApplication
public class WebCrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebCrmApplication.class, args);
	}

}
