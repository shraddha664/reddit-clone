package com.saru.redditclone.controller;

import com.saru.redditclone.dto.AuthenticationResponse;
import com.saru.redditclone.dto.LoginRequest;
import com.saru.redditclone.dto.RegisterRequest;
import com.saru.redditclone.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody RegisterRequest registerRequest){
        authService.signup(registerRequest);
        return new ResponseEntity<>("User registration succesfully", HttpStatus.OK);
    }

    @GetMapping("accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token)  {

        authService.verifyAccount(token);
        return new ResponseEntity<>("Account activated succesfully",HttpStatus.OK);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody LoginRequest loginRequest){
        log.info("inside login");
   return authService.login(loginRequest);
    }
}
