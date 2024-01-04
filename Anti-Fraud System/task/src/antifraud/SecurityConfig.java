package antifraud;

import antifraud.user.User;
import antifraud.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;

@Configuration
public class SecurityConfig {
    private static final String MERCHANT = User.Role.MERCHANT.name();
    private static final String ADMINISTRATOR = User.Role.ADMINISTRATOR.name();
    private static final String SUPPORT = User.Role.SUPPORT.name();
    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint restAuthenticationEntryPoint;
        return http
                .httpBasic(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)                           // For modifying requests via Postman
                .exceptionHandling(handing -> handing
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                            }
                        }) // Handles auth error
                )
                .headers(headers -> headers.frameOptions().disable())           // for Postman, the H2 console
                .authorizeHttpRequests(requests -> requests                     // manage access
                        .requestMatchers("/actuator/shutdown").permitAll()      // needs to run test
                        .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole(ADMINISTRATOR, SUPPORT)
                        .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*").hasRole(ADMINISTRATOR)
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole(MERCHANT)
                        .requestMatchers(HttpMethod.PUT, "/api/auth/access", "/api/auth/role").hasRole(ADMINISTRATOR)
                        .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip", "/api/antifraud/stolencard").hasRole(SUPPORT)
                        .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/*", "/api/antifraud/stolencard/*").hasRole(SUPPORT)
                        .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip", "/api/antifraud/stolencard", "/api/antifraud/history/**").hasRole(SUPPORT)
                        .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole(SUPPORT)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                // other configurations
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                username = username.toLowerCase();
                User user = userRepository.findByUsername(username);
                return org.springframework.security.core.userdetails.User
                        .withUsername(username)
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .disabled(user.isLocked())
                        .build();
            }
        };
    }
}
