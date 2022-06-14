package com.alex.springboot.app;

import com.alex.springboot.app.models.services.IUploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class SpringbootJpaApplication implements CommandLineRunner {
	@Autowired
	IUploadFileService uploadFileService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		ApplicationContext context=SpringApplication.run(SpringbootJpaApplication.class, args);

	}

	@Override
	public void run(String... args) throws Exception {
		uploadFileService.deleteAll();
		uploadFileService.init();
		String password = "12345";
		for (int i = 0; i < 2; i++) {
			String bycryptPass = passwordEncoder.encode(password);
			System.out.println(bycryptPass);
		}
	}
}
