package com.saru.redditclone.service;

import com.saru.redditclone.dto.AuthenticationResponse;
import com.saru.redditclone.dto.LoginRequest;
import com.saru.redditclone.dto.RegisterRequest;
import com.saru.redditclone.exception.SpringRedditException;
import com.saru.redditclone.model.NotificationEmail;
import com.saru.redditclone.model.User;
import com.saru.redditclone.model.VerificationToken;
import com.saru.redditclone.repository.UserRepository;
import com.saru.redditclone.repository.VerificationTokenRepository;
import com.saru.redditclone.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    private final MailService mailService;
    private final AuthenticationManager authenticationManager;

    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(RegisterRequest registerRequest){
        User user=User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .created(Instant.now())
                .enabled(false)
                .build();
        userRepository.save(user);

      String token=generateVerificationToken(user);
      mailService.sendMail(new NotificationEmail("Please Activate your account", user.getEmail(),
              "Thank you for signing up to Spring reddit"+"please click the link below to activate your account:"+"http://localhost:8080/api/auth/accountVerification/"+token));
    }

    private String generateVerificationToken(User user) {
       String verificationToken= UUID.randomUUID().toString();
        VerificationToken verificationToken1=new VerificationToken();
        verificationToken1.setToken(verificationToken);
        verificationToken1.setUser(user);

        verificationTokenRepository.save(verificationToken1);
        return verificationToken;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken>verificationToken=verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(()->new SpringRedditException("Invalid Token"));
        fetchUserAndEnablle(verificationToken.get());

    }

    @Transactional
    private void fetchUserAndEnablle(VerificationToken verificationToken) {
        String username=verificationToken.getUser().getUsername();
        User user= userRepository.findByUsername(username).orElseThrow(()->new SpringRedditException("user not found with name"+username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token=jwtProvider.generateToken(authenticate);
        return new AuthenticationResponse(token, loginRequest.getUsername());
    }
}
