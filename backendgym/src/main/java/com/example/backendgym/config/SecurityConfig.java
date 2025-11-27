package com.example.backendgym.config;

// imports:
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.example.backendgym.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/register-init",
                    "/api/auth/register-confirm",
                    "/api/auth/login",
                    "/api/auth/login-init",
                    "/api/auth/login-confirm",
                    "/api/auth/recover-init",
                    "/api/auth/recover-confirm",
                    "/api/auth/recover-validate"
                ).permitAll()
                // Públicos de lectura
                .requestMatchers(HttpMethod.GET,
                        "/api/productos/**",
                        "/api/promociones/**",
                        "/api/reservas/filtros/uso",
                        "/api/reservas/productos"
                ).permitAll()

                // Mercado Pago: permitir temporalmente para pruebas
                .requestMatchers("/api/pagos/mercadopago/**").permitAll()

                // Administración de pagos/pedidos
                .requestMatchers("/api/pagos/admin/**").hasAnyRole("TRABAJADOR", "ADMIN")

                // Check-in por QR: solo trabajador o admin
                .requestMatchers("/api/checkin/**").hasAnyRole("TRABAJADOR", "ADMIN")

                // Usuario autenticado (USUARIO/TRABAJADOR/ADMIN)
                .requestMatchers(
                        "/api/dashboard/**",
                        "/api/carrito/**",
                        "/api/pagos/**",
                        "/api/reservas/**",
                        "/api/membresias/**",
                        "/api/planes/**",
                        "/api/consultas/**",
                        "/api/perfil/**"
                ).hasAnyRole("USUARIO", "TRABAJADOR", "ADMIN")

                // Gestión de productos y promociones (solo TRABAJADOR o ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/promociones/**").hasAnyRole("TRABAJADOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/promociones/**").hasAnyRole("TRABAJADOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/promociones/**").hasAnyRole("TRABAJADOR", "ADMIN")

                // Responder consultas: trabajador o admin
                .requestMatchers(HttpMethod.POST, "/api/consultas/*/responder").hasAnyRole("TRABAJADOR", "ADMIN")

                .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"unauthorized\"}");
            }))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Permitir frontend local y dominios de túneles (ngrok/localtunnel) en pruebas
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "https://*.ngrok.app",
                "https://*.loca.lt"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

