package com.hbc.tickets.repository;

import com.hbc.tickets.model.Role;
import com.hbc.tickets.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User findByUsername(String username);
    User findByEmail(String mail);
    List<User> findAll();
	List<User> findByRoleIn(List<Role> of);
}
