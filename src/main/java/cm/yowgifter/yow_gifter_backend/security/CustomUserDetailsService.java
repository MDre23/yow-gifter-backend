package cm.yowgifter.yow_gifter_backend.security;

import cm.yowgifter.yow_gifter_backend.domain.User;
import cm.yowgifter.yow_gifter_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable avec l'identifiant : " + usernameOrEmail));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
