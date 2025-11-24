package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.LoginRequest;
import cl.huertohogar.huertohogar_api.dto.RegisterRequest;
import cl.huertohogar.huertohogar_api.exception.BadRequestException;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(authService, "jwtSecret", "mySuperSecretKeyThatIsLongEnoughForJWT256BitsOrMoreToWorkProperly");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setNombre("Test");
        request.setApellido("User");
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setDireccion("Test Address");
        request.setTelefono("123456789");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return user;
        });

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailExists_ThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(BadRequestException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRol("cliente");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", user.getPassword())).thenReturn(true);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        var response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test@example.com", response.getUser().getEmail());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException.class, () ->
            authService.login(loginRequest));
    }
}
