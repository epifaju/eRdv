package com.erdv.config;

import com.erdv.service.AuthRateLimitFilter;
import com.erdv.service.JwtAuthenticationFilter;
import com.erdv.service.JwtService;
import com.erdv.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Les motifs {@code requestMatchers(String)} délèguent à {@code MvcRequestMatcher} dès que Spring MVC
 * est présent : certains chemins peuvent alors ne pas matcher et tomber dans des règles inattendues.
 * On utilise explicitement {@link org.springframework.security.web.util.matcher.AntPathRequestMatcher}
 * pour des chemins servlet stables (hors context-path {@code /api}).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Value("${app.openapi.enabled:false}")
    private boolean openApiEnabled;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, utilisateurService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            AuthRateLimitFilter authRateLimitFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                            .requestMatchers(antMatcher("/actuator/health"), antMatcher("/actuator/health/**")).permitAll()
                            .requestMatchers(antMatcher("/actuator/prometheus")).hasRole("ADMIN")
                            .requestMatchers(antMatcher("/error")).permitAll()
                            .requestMatchers(antMatcher("/auth/logout-all")).authenticated()
                            .requestMatchers(antMatcher("/auth/**")).permitAll()
                            .requestMatchers(antMatcher(HttpMethod.GET, "/etablissements")).permitAll()
                            .requestMatchers(antMatcher(HttpMethod.GET, "/etablissements/*")).permitAll()
                            .requestMatchers(antMatcher(HttpMethod.GET, "/etablissements/*/prestataires")).permitAll()
                            .requestMatchers(antMatcher("/etablissements/**")).hasRole("ADMIN")
                            .requestMatchers(antMatcher("/prestataires")).permitAll()
                            .requestMatchers(antMatcher("/prestataires/*")).permitAll()
                            .requestMatchers(antMatcher("/prestataires/**")).hasRole("ADMIN")
                            .requestMatchers(antMatcher("/prestations/prestataire/*")).permitAll()
                            .requestMatchers(antMatcher("/prestations/**")).hasAnyRole("ADMIN", "PRESTATAIRE")
                            .requestMatchers(antMatcher("/plages-recurrentes/**")).hasAnyRole("ADMIN", "PRESTATAIRE")
                            .requestMatchers(antMatcher("/creneaux/prestataire/*/disponibles")).authenticated()
                            .requestMatchers(antMatcher("/creneaux/prestataire/*/disponibles/date")).authenticated()
                            .requestMatchers(antMatcher("/creneaux/**")).hasRole("ADMIN")
                            .requestMatchers(antMatcher(HttpMethod.GET, "/rendez-vous/tous")).hasRole("ADMIN")
                            .requestMatchers(antMatcher("/rendez-vous/mon-agenda")).hasRole("PRESTATAIRE")
                            .requestMatchers(antMatcher("/rendez-vous/prestataire/*")).hasAnyRole("ADMIN", "PRESTATAIRE")
                            .requestMatchers(antMatcher("/rendez-vous")).authenticated()
                            .requestMatchers(antMatcher("/rendez-vous/**")).authenticated();
                    if (openApiEnabled) {
                        auth.requestMatchers(
                                antMatcher("/v3/api-docs/**"),
                                antMatcher("/swagger-ui/**"),
                                antMatcher("/swagger-ui.html")).permitAll();
                    }
                    auth.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(utilisateurService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
