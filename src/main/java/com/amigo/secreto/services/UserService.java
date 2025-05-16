package com.amigo.secreto.services;

import com.amigo.secreto.models.Group;
import com.amigo.secreto.models.User;
import com.amigo.secreto.repositories.UserRepository;
import com.amigo.secreto.services.exceptions.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).
                orElseThrow(() -> new UsernameNotFoundException("Usuário " + username + " não encontrado"));
    }

    public User update(User user) {
        return userRepository.save(user);
    }

    public void delete(UUID id) {
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de id " + id + " não encontrado"));

        userRepository.deleteById(id);
    }

    public int countParticipatingGroups(UUID userId) {
        return userRepository.countParticipatingGroups(userId);
    }

    public int countParticipatingDraws(UUID userId) {
        return userRepository.countParticipatingDraws(userId);
    }

    public int participatingGroups() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return countParticipatingGroups(currentUser.getId());
    }

    public int participatingDraws() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        return countParticipatingDraws(currentUser.getId());
    }
}
