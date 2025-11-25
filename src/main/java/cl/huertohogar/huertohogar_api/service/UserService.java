package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.RegisterRequest;
import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Role;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(String email) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserResponse(user);
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::new);
    }

    public User getUserEntityById(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserResponse createUserFromAdmin(RegisterRequest request) {
        if (userRepository.existsById(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setRun(request.getRun());
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellidos());
        user.setDireccion(request.getDireccion());
        user.setTelefono(request.getTelefono());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRol(Role.USER);

        User saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    public UserResponse updateUserFromAdmin(String email, RegisterRequest request) {
        User user = userRepository.findById(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setRun(request.getRun());
        user.setNombre(request.getNombre());
        user.setApellido(request.getApellidos());
        user.setDireccion(request.getDireccion());
        user.setTelefono(request.getTelefono());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updated = userRepository.save(user);
        return new UserResponse(updated);
    }

    public void deleteUser(String email) {
        if (!userRepository.existsById(email)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepository.deleteById(email);
    }
}
