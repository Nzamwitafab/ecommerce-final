package com.ecommerce;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ecommerce.Repository.UserRepository;
import com.ecommerce.entities.User;


@SpringBootApplication
@ComponentScan(basePackages = "com.ecommerce.*")
public class EcommerceApplication implements CommandLineRunner {
	
	@Autowired
	public BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepo;

	public static void main(String[] args) {
		SpringApplication.run(EcommerceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Check if the user already exists
		String email = "admin@e-commerce.com";
		Optional<User> existingUser = userRepo.findByEmail(email); // Assuming you have a method to find by email

		if (existingUser.isEmpty()) { // User does not exist, so create a new one
			User user = new User();
			user.setId(1);
			user.setEmail(email);
			user.setEnable(true);
			user.setName("Fabrice");
			user.setPhone("1234567890");
			user.setRole("ROLE_ADMIN");
			user.setPassword(passwordEncoder.encode("admin"));
			user.setProfile("admin.png");
			user.setDate(new Date());

			userRepo.save(user);
		} else {
			System.out.println("User already exists: " + email);
		}
	}


}
