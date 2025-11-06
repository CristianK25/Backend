package org.springej.backende_commerce.config;

import lombok.RequiredArgsConstructor;
import org.springej.backende_commerce.security.AuthEntryPoint;
import org.springej.backende_commerce.security.JwtFilter;
import org.springej.backende_commerce.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPoint authEntryPoint;
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 🔹 Desactivar CSRF (usamos JWT)
        http.csrf(csrf -> csrf.disable());

        // 🔹 Habilitar CORS con la configuración de WebConfig o application.properties
        http.cors(Customizer.withDefaults());

        // 🔹 Definir reglas de acceso
        http.authorizeHttpRequests(auth -> auth
                // === RUTAS PÚBLICAS ===
                .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
                .requestMatchers("/api/chat", "/api/price").permitAll()

                // === MERCADO PAGO ===
                .requestMatchers("/api/payments/webhook", "/api/payments/success", "/api/payments/failure", "/api/payments/pending").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/payments/create-order").authenticated()

                // === USUARIOS Y VENTAS ===
                .requestMatchers(HttpMethod.POST, "/api/ventas").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/ventas/mis-compras").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/ventas", "/api/ventas/usuario/**").hasRole("ADMIN")

                .requestMatchers("/api/favoritos/**", "/api/estrellas/**").authenticated()

                // === ADMIN ===
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                .requestMatchers("/api/producto-imagenes/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/usuarios/domicilios").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/domicilios/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")

                // === RESTO DE RUTAS ===
                .anyRequest().authenticated()
        );

        // 🔹 Manejo de errores
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint));

        // 🔹 Filtro JWT antes de UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
