package com.example.spring.configserver.client.demo.PCFConfigServerClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class PcfConfigServerClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(PcfConfigServerClientApplication.class, args);
	}

	@Order(105)
	@Profile("cloud")
	@Configuration
	static class ApplicationSecurityOverride extends WebSecurityConfigurerAdapter {

		@Override
		public void configure(HttpSecurity web) throws Exception {
			web.authorizeRequests().anyRequest().permitAll().and().httpBasic() .disable();
			web.csrf().disable();
		}
	}

}
