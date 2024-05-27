package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.Repository.UserRepository;
import com.ecommerce.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = this.userRepo.loadUserByUserName(username);
		
		if(user==null) {
			throw new UsernameNotFoundException("No user is available for this email address.");
		}
		
		return new CustomUserDetails(user);
	}

}
