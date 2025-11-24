package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.UserResponse;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.User;
import cl.huertohogar.huertohogar_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
}
