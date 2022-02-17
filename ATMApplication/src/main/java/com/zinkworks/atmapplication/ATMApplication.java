package com.zinkworks.atmapplication;

import com.zinkworks.atmapplication.model.atm.ATM;
import com.zinkworks.atmapplication.model.atm.Currency;
import com.zinkworks.atmapplication.model.customer.CustomerAccount;
import com.zinkworks.atmapplication.repository.atm.ATMRepository;
import com.zinkworks.atmapplication.repository.atm.CurrencyRepository;
import com.zinkworks.atmapplication.repository.customer.CustomerAccountRepository;
import com.zinkworks.atmapplication.security.ATMBasicAuthenticationEntryPoint;
import com.zinkworks.atmapplication.security.UserDetailsServiceImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Slf4j
@EnableWebSecurity
@ComponentScan({"com.zinkworks.atmapplication.*"})
@EnableJpaRepositories(basePackages = {"com.zinkworks.atmapplication.repository.*"})
@EntityScan({"com.zinkworks.atmapplication.model*"})
public class ATMApplication {

	public static void main(String[] args) {
		SpringApplication.run(ATMApplication.class, args);
	}

	@Bean
	CommandLineRunner initializeData(
			@Autowired ATMRepository atmRepository,
			@Autowired CurrencyRepository currencyRepository,
			@Autowired CustomerAccountRepository customerAccountRepository) {
		return args -> {


			ATM atm = ATM.builder()
					.ATMBalance("5000")
					.ATMName("Fergals ATM")
					.build();

			//The Following Code Initialises the ATM to contain Currency to the amount of €1500
			atmRepository.save(atm);
			for (int i = 0; i < 30; i++) {
				currencyRepository.save(Currency.builder().currency("EURO").name("TWENTY").value(20).atm(atm).build());
			}
			for (int i = 0; i < 10; i++) {
				currencyRepository.save(Currency.builder().currency("EURO").name("FIFTY").value(50).atm(atm).build());
			}
			for (int i = 0; i < 30; i++) {
				currencyRepository.save(Currency.builder().currency("EURO").name("TEN").value(10).atm(atm).build());
			}
			for (int i = 0; i < 20; i++) {
				currencyRepository.save(Currency.builder().currency("EURO").name("FIVE").value(5).atm(atm).build());
			}

			//Here the Customer Account is initialised with account balance and PinCode stored with strong bcrypt hash of the Pin Code.
			customerAccountRepository.save(CustomerAccount.builder().accountId("1234").accountBalance(300).pin("$2a$11$h2rsjI3WrUcHAjeie7mHmua5Mh812kIyibni1NAPU2FuJKLy9qWXW").build());
			customerAccountRepository.save(CustomerAccount.builder().accountId("1235").accountBalance(10).pin("$2a$11$h2rsjI3WrUcHAjeie7mHmua5Mh812kIyibni1NAPU2FuJKLy9qWXW").build());
			customerAccountRepository.save(CustomerAccount.builder().accountId("1236").accountBalance(40).pin("$2a$11$h2rsjI3WrUcHAjeie7mHmua5Mh812kIyibni1NAPU2FuJKLy9qWXW").build());
			customerAccountRepository.save(CustomerAccount.builder().accountId("1237").accountBalance(90).pin("$2a$11$h2rsjI3WrUcHAjeie7mHmua5Mh812kIyibni1NAPU2FuJKLy9qWXW").build());
			customerAccountRepository.save(CustomerAccount.builder().accountId("1238").accountBalance(350).pin("$2a$11$h2rsjI3WrUcHAjeie7mHmua5Mh812kIyibni1NAPU2FuJKLy9qWXW").build());
		};
	}

	@Configuration
	@Order(1)
	@EnableWebSecurity
	@ComponentScan("com.zinkworks.atmapplication.*")
	/*ATM Requests are Authorised by validating the users data against the Database*/
	public class ATMAppSecurityConfig extends WebSecurityConfigurerAdapter {

		@Autowired
		private WebApplicationContext applicationContext;

		@Autowired
		ATMBasicAuthenticationEntryPoint atmBasicAuthenticationEntryPoint;

		@Autowired
		private UserDetailsServiceImp userDetailsService;

		@PostConstruct
		public void completeSetup() {
			userDetailsService = applicationContext.getBean(UserDetailsServiceImp.class);
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {

			http.antMatcher("/atm/**").authorizeRequests().anyRequest().authenticated()
					.and().httpBasic();
		}

		@Override
		protected void configure(final AuthenticationManagerBuilder auth) {
			auth.authenticationProvider(authenticationProvider());
		}

		@Bean
		public DaoAuthenticationProvider authenticationProvider() {

			log.info("Setting the UserDetailsService in Auth Provider");
			final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
			authProvider.setUserDetailsService(userDetailsService);
			authProvider.setPasswordEncoder(encoder());
			return authProvider;
		}


		@Bean
		public PasswordEncoder encoder() {
			return new BCryptPasswordEncoder(11);
		}

		@Bean
		public SecurityEvaluationContextExtension securityEvaluationContextExtension() {
			return new SecurityEvaluationContextExtension();
		}

		@Bean
		@Override
		public UserDetailsService userDetailsService() {
			return new UserDetailsServiceImp();
		}
	}

	@Configuration
	@Order(2)
	/*Unauthenticated URL to allow heartbeat check of application*/
	public static class HeartbeatSecurityConfig extends WebSecurityConfigurerAdapter {

		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/heartbeat/**").authorizeRequests().anyRequest().permitAll();

		}
	}


}





