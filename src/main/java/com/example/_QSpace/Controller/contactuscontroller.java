package com.example._QSpace.Controller;

import com.example._QSpace.Entity.Contactus;
import com.example._QSpace.Repository.contactusrepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class contactuscontroller {
    
    private final contactusrepo repo;
    
    public contactuscontroller(contactusrepo repo) {
        this.repo = repo;
    }
    
    // ========== METHOD 1: Save contact form (Existing) ==========
    @PostMapping("contactus")
    public ResponseEntity<Map<String, String>> addContactus(@RequestBody Contactus contactus) {
        repo.save(contactus);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contact saved successfully");
        return ResponseEntity.ok(response);
    }
    
    // ========== METHOD 2: View all rows ==========
    @GetMapping("/contactus/all")
    public ResponseEntity<List<Contactus>> getAllContacts() {
        List<Contactus> allContacts = repo.findAll();
        return ResponseEntity.ok(allContacts);
    }
    
    // ========== METHOD 3: Search by email (View one row) ==========
    @GetMapping("/contactus/search/{email}")
    public ResponseEntity<?> getContactByEmail(@PathVariable String email) {
        if (repo.existsById(email)) {
            Contactus contact = repo.findById(email).get();
            return ResponseEntity.ok(contact);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No contact found with email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // ========== METHOD 4: Delete one row by email ==========
    @DeleteMapping("/contactus/delete/{email}")
    public ResponseEntity<Map<String, String>> deleteContactByEmail(@PathVariable String email) {
        Map<String, String> response = new HashMap<>();
        
        if (repo.existsById(email)) {
            repo.deleteById(email);
            response.put("message", "Contact with email " + email + " deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "No contact found with email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    // ========== METHOD 5: Delete all rows ==========
    @DeleteMapping("/contactus/delete/all")
    public ResponseEntity<Map<String, String>> deleteAllContacts() {
        repo.deleteAll();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All contacts deleted successfully");
        return ResponseEntity.ok(response);
    }
}

