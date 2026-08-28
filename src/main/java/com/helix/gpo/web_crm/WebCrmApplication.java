package com.helix.gpo.web_crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;

@Modulithic(sharedModules = "shared")
@EnableScheduling
@SpringBootApplication
public class WebCrmApplication {

	public static void main(String[] args) {
		Locale.setDefault(Locale.GERMANY);
		SpringApplication.run(WebCrmApplication.class, args);
	}

}
