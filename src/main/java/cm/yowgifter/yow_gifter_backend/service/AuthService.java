package cm.yowgifter.yow_gifter_backend.service;

import cm.yowgifter.yow_gifter_backend.domain.User;
import cm.yowgifter.yow_gifter_backend.repository.UserRepository;
import cm.yowgifter.yow_gifter_backend.security.JwtService;
import cm.yowgifter.yow_gifter_backend.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà avec cette adresse email.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce pseudonyme est déjà utilisé.");
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername().trim())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .phone(request.getPhone())
                .city(request.getCity() != null ? request.getCity() : "Yaoundé")
                .neighborhood(request.getNeighborhood())
                .age(request.getAge())
                .bio(request.getBio())
                .isVerified(false)
                .build();

        user = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));
        return mapToUserDto(user);
    }

    @Transactional
    public UserDto updateProfile(String email, UserDto updateDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable."));

        if (updateDto.getFirstName() != null) user.setFirstName(updateDto.getFirstName().trim());
        if (updateDto.getNeighborhood() != null) user.setNeighborhood(updateDto.getNeighborhood().trim());
        if (updateDto.getAge() != null) user.setAge(updateDto.getAge());
        if (updateDto.getBio() != null) user.setBio(updateDto.getBio().trim());
        if (updateDto.getAvatarUrl() != null) user.setAvatarUrl(updateDto.getAvatarUrl());

        user = userRepository.save(user);
        return mapToUserDto(user);
    }

    public UserPublicDto getPublicUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profil citoyen introuvable."));

        return UserPublicDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .city(user.getCity())
                .neighborhood(user.getNeighborhood())
                .age(user.getAge())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .city(user.getCity())
                .neighborhood(user.getNeighborhood())
                .age(user.getAge())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .isVerified(user.getIsVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
