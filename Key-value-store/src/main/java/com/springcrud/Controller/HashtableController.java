package com.springcrud.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.springcrud.Model.Hashtable;
import com.springcrud.Repo.HashtableRepo;

import java.util.List;

@RestController
public class HashtableController {

    @Autowired
    private HashtableRepo hashtableRepo;

    @GetMapping(value = "/hashtable/{key}")
    public ResponseEntity<String> getHashtable(@PathVariable String key) {
        
        return hashtableRepo.findById(key)
                .map(hashtable -> ResponseEntity.ok(hashtable.getValue())) // Return the 'value' if found
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record not found"));
    }


    @GetMapping(value = "/hashtables")
    public List<Hashtable> getAllHashtables() {
        return hashtableRepo.findAll();
    }

    @PostMapping(value = "/hashtable")
    public ResponseEntity<String> save(@RequestBody Hashtable hashtable) {
        hashtableRepo.save(hashtable);
        return ResponseEntity.status(HttpStatus.CREATED).body("Hashtable entry saved successfully");
    }
    @PutMapping(value = "/hashtable/{key}")
    public ResponseEntity<String> update(@PathVariable String key, @RequestBody Hashtable updatedHashtable) {
        // Attempt to update only if expiredAt > NOW()
        System.out.println("--------------------------");
        
        System.out.println(key +" "+updatedHashtable.getValue()+ " "+updatedHashtable.getExpiredAt());
        int updatedRows = hashtableRepo.upsertHashtable(key, updatedHashtable.getValue(), updatedHashtable.getExpiredAt());
        
        if (updatedRows > 0) {
            return ResponseEntity.ok("Record updated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Update failed: Record expired or not found");
        }
    }
    

    @DeleteMapping(value = "/hashtable/{key}")
    public ResponseEntity<String> delete(@PathVariable String key) {
        return hashtableRepo.findById(key)
                .map(existingHashtable -> {
                    hashtableRepo.delete(existingHashtable);
                    return ResponseEntity.ok("Record deleted successfully");
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Record not found"));
    }
}
