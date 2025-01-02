package com.springcrud.Controller;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.springcrud.Model.User;
import com.springcrud.Repo.UserRepo;
import reactor.core.publisher.Flux;


@RestController
public class ApiController {
    
    @Autowired
    private UserRepo userRepo;


    @GetMapping(value = "/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {
        
        return userRepo.findById(id)
            .map(ResponseEntity::ok) // Return User if found
            .orElseGet(() -> ResponseEntity.badRequest().body(null));
    }
    
    @GetMapping(value = "/users")
    public List<User> getUsers()
    {
        return userRepo.findAll();
    }

    @PostMapping(value = "/save")
    public String save(@RequestBody User user) {
        
        userRepo.save(user);
        return "Saved..!";
    }

    @PutMapping("path/{id}")
    public ResponseEntity<User> update(@PathVariable Integer id) {

       return userRepo.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
        
    }

    @DeleteMapping(value = "/delete/{id}")
    public String delete(@PathVariable int id)
    {
         userRepo.deleteById(id);

         return "Deleted..!";
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Boolean> updateUser(@PathVariable Integer id, @RequestBody User updatedUser) {

        return userRepo.findById(id)
                .map(existingUser -> {
                   
                    existingUser.setFirstName(updatedUser.getFirstName());
                    existingUser.setLastName(updatedUser.getLastName());
                    existingUser.setEmail(updatedUser.getEmail());
                   
                    userRepo.save(existingUser);
                    return ResponseEntity.ok(true);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(false));
    }

    @GetMapping(value = "/sse-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvents() {
        // Generate a stream of data
        return Flux.interval(Duration.ofSeconds(1))
                   .map(sequence -> "Event number: " + sequence);
    }
    
}
