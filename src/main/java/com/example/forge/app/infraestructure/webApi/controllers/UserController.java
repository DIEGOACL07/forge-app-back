package com.example.forge.app.infraestructure.webApi.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.forge.app.application.services.UserService;
import com.example.forge.app.domain.dto.UserDto;
import com.example.forge.app.domain.entities.UserEntity;
import com.example.forge.app.domain.response.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired
  private UserService userService;

  public class CustomErrorResponse {
    private String message;

    public CustomErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

  private ResponseEntity<?> validation(BindingResult result) {
    Map<String, String> errors = new HashMap<>();

    result.getFieldErrors().forEach(err -> {
      errors.put(err.getField(), "El campo "+err.getField()+" "+err.getDefaultMessage());
    });
    return ResponseEntity.badRequest().body((errors));
  }

  @GetMapping
  public List<UserDto> list() {
    return userService.findAll();
  }

  @GetMapping("{id}")
  public ResponseEntity<?> show(@PathVariable Long id) {
    Optional<UserDto> userOptional = userService.findById(id);
    if(userOptional.isPresent()) {
      return ResponseEntity.ok(userOptional.orElseThrow());
    }
    return ResponseEntity.notFound().build();
  };

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> create(@Valid @RequestBody UserEntity user, BindingResult result) {

    if(result.hasErrors()){
      return this.validation(result);
    }

    try {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(user));
    } catch (DataIntegrityViolationException e) {
      String errorMessage = "El email o el username ya están en uso.";
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", errorMessage);
      return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
  }

  @PutMapping("{id}")
  public ResponseEntity<?> update(@Valid @RequestBody UserResponse user, Long id, BindingResult result) {
    Optional<UserDto> o = userService.update(user, id);
    if(o.isPresent()){
      return ResponseEntity.status(HttpStatus.CREATED).body(o.orElseThrow());
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("{id}")
  public ResponseEntity<?> remove(@PathVariable() Long id) {
    Optional<UserDto> o = userService.findById(id);
    if(o.isPresent()) {
      userService.remove(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
