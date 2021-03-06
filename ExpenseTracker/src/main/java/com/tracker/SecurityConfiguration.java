package com.tracker;

import javax.servlet.MultipartConfigElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.tracker.services.UsersService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled=true, prePostEnabled=true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter  {
	@Autowired
	BCryptPasswordEncoder bcryptEncoder;
	 
	@Autowired
	private UsersService detailsService;
	
	@Autowired
	public void configAuthBuilder(AuthenticationManagerBuilder authBuilder) throws Exception {
		authBuilder.userDetailsService(detailsService)
        			.passwordEncoder(bcryptEncoder);
	}
	@Bean
	public BCryptPasswordEncoder passwordEncoder(){
		BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();
		return encoder;
	}
	
	@Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("20MB");
        factory.setMaxRequestSize("20MB");
        return factory.createMultipartConfig();
    }
	
	protected void configure(HttpSecurity http) throws Exception {
		
		http
		  	.exceptionHandling()
		.and()
			.authorizeRequests()	
				.antMatchers("/resources/**","/").permitAll()
				.antMatchers("/initDatabase").permitAll()
				.anyRequest().authenticated()
		.and()
			.formLogin()
				.loginPage("/")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl("/home")		
				.failureUrl("/?error=true")
				.usernameParameter("username")
				.passwordParameter("password")
				.permitAll()
		.and()
			.logout()
				.invalidateHttpSession(true)
				.logoutUrl("/logout")
				.logoutSuccessUrl("/logoutSuccess");
		
		http.csrf().disable();
	}
	
	@Autowired
	protected void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {	  
	    auth.userDetailsService(detailsService);
	}
}
