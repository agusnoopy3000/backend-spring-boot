package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.AuthResponse;
import cl.huertohogar.huertohogar_api.dto.LoginRequest;
import cl.huertohogar.huertohogar_api.dto.RegisterRequest;
import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.exception.BadRequestException;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellido());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDireccion(request.getDireccion());
        user.setTelefono(request.getTelefono());
        user.setRol("cliente"); // Default role
        user = userRepository.save(user);
        String token = generateToken(user);
        return new AuthResponse(token, new UserResponse(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String token = generateToken(user);
        return new AuthResponse(token, new UserResponse(user));
    }

    private String generateToken(User user) {
        // jjwt 0.9.1: use .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRol())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                .compact();
    }
}
