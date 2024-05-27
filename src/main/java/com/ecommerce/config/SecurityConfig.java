package com.ecommerce.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.ecommerce.Repository.UserRepository;
import com.ecommerce.entities.User;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserRepository userRepo;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public UserDetailsServiceImpl getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/admin/**").hasRole("ADMIN")
				.antMatchers("/customer/**").hasRole("CUSTOMER")
				.antMatchers("/seller/**").hasAnyRole("SELLER", "ADMIN")
				.antMatchers("/**").permitAll()
				.and()
				.formLogin()
				.successHandler(new AuthenticationSuccessHandler() {
					@Override
					public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
														Authentication authentication) throws IOException, ServletException {
						User user = userRepo.loadUserByUserName(authentication.getName());
						String redirectURL = request.getContextPath();
						if(user.getRole().equals("ROLE_CUSTOMER")) {
							redirectURL = "/home";
						} else if(user.getRole().equals("ROLE_SELLER")) {
							redirectURL = "/seller/home";
						} else if(user.getRole().equals("ROLE_ADMIN")) {
							redirectURL = "/admin/home";
						}
						response.sendRedirect(redirectURL);
					}
				})
				.failureHandler(new AuthenticationFailureHandler() {
					@Override
					public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
														AuthenticationException exception) throws IOException, ServletException {
						HttpSession httpSession = request.getSession();
						if(exception.getMessage().equals("Bad credentials")) {
							httpSession.setAttribute("status", "bad-credentials");
							response.sendRedirect("/login?=BadCredentials");
						} else if(exception.getMessage().equals("User is disabled")) {
							httpSession.setAttribute("status", "user-disabled");
							response.sendRedirect("/login?=AccountSuspended");
						}
					}
				})
				.loginPage("/login")
				.and()
				.logout()
				.logoutSuccessHandler(new LogoutSuccessHandler() {
					@Override
					public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
							throws IOException, ServletException {
						HttpSession httpSession = request.getSession();
						if(authentication != null) {
							httpSession.setAttribute("status", "logout-success");
							response.sendRedirect("/login?logoutSuccess");
						} else {
							response.sendRedirect("/login?doLogin");
						}
					}
				})
				.and()
				.csrf()
				.disable();
	}
}
