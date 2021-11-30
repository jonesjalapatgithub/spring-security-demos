package com.jones.springsecurity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jones.springsecurity.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
	
    Optional<User> findByUserName(String userName);

}