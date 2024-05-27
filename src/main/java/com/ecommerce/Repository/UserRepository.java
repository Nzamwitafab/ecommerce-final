package com.ecommerce.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecommerce.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
	
	
	@Query("select u from User u where u.email =:email")
	public User loadUserByUserName(@Param("email") String email);
	
	@Query(value = "select * from users", nativeQuery = true)
	public List<User> getUsers();
	Optional<User> findByEmail(String email);

}
	