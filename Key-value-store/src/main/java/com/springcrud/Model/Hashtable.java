package com.springcrud.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Data
@Table(name = "hashtable")
@Getter
@Setter
public class Hashtable {

    @Id
    @Column(name = "my_key", nullable = false, length = 255)  // Updated to myKey
    private String key;

    @JsonProperty("value")
    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @JsonProperty("expiredAt")
    @Column(name = "expiredAt", nullable = false)
    private Long expiredAt;
}
