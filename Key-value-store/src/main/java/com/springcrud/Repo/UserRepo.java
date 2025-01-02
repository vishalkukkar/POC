package com.springcrud.Repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springcrud.Model.User;

public interface UserRepo extends JpaRepository<User, Integer>{

}
