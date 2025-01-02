package com.springcrud.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name = "User")
@Getter
@Setter
public class User {

    @Id
    @Column(name= "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonProperty("firstName")
    @Column(name="first_name")
    private String firstName;

    @JsonProperty("lastName")
    @Column(name="last_name")
    private String lastName;

    @JsonProperty("email")
    @Column(name = "email")
    private String email;
}
