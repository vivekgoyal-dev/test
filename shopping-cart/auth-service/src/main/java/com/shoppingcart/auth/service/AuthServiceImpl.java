package com.shoppingcart.auth.service;

import com.shoppingcart.auth.config.JwtUtil;
import com.shoppingcart.auth.dto.LoginRequest;
import com.shoppingcart.auth.dto.LoginResponse;
import com.shoppingcart.auth.dto.RegisterRequest;
import com.shoppingcart.auth.dto.UserDto;
import com.shoppingcart.auth.exception.ApiException;
import com.shoppingcart.auth.model.UserAccount;
import com.shoppingcart.auth.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserAccountRepository repository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDto register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("email already registered: " + request.getEmail());
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setActive(true);
        return toDto(repository.save(user));
    }

    /**
     * The same message for a wrong email and a wrong password, deliberately: telling the caller
     * which half was wrong tells them which emails are registered.
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        UserAccount user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("invalid email or password");
        }
        if (!user.isActive()) {
            throw ApiException.unauthorized("this account has been deactivated");
        }
        return new LoginResponse(jwtUtil.generateToken(user), user.getUserId(), user.getEmail(), user.getRole());
    }

    @Override
    public List<UserDto> getAllUsers() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public UserDto deactivate(Long userId) {
        UserAccount user = repository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("no user " + userId));
        user.setActive(false);
        return toDto(repository.save(user));
    }

    private UserDto toDto(UserAccount user) {
        return new UserDto(user.getUserId(), user.getEmail(), user.getRole(), user.isActive());
    }
}
