package com.example.backendgym.security;

import com.example.backendgym.domain.Usuario;
import com.example.backendgym.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UsuarioRepository repo;
  public CustomUserDetailsService(UsuarioRepository repo) { this.repo = repo; }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    Usuario u = repo.findByEmail(email).orElseThrow(
        () -> new UsernameNotFoundException("No existe usuario con email: " + email)
    );
    return new User(u.getEmail(), u.getPassword(),
        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol().name())));
  }
}
