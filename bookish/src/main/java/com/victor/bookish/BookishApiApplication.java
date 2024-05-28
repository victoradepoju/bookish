package com.victor.bookish;

import com.victor.bookish.role.Role;
import com.victor.bookish.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableJpaRepositories(basePackages =
		{"com.victor.bookish.user",
				"com.victor.bookish.role",
				"com.victor.bookish.book",
				"com.victor.bookish.feedback",
				"com.victor.bookish.history"
		})

public class BookishApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookishApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner(RoleRepository roleRepository) {
		return  args -> {
			if(roleRepository.findByName("USER").isEmpty()) {
				roleRepository.save(
						Role.builder().name("USER").build()
				);
			}
		};
	}
}
