package com.amigo.secreto.services;

import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + id + " não encontrado")));
    }

    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Usuário de email " + email + " não encontrado")));
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + id + " não encontrado"));

        userRepository.deleteById(id);
    }

}
