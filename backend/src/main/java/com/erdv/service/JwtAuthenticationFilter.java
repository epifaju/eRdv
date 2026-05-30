package com.erdv.service;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;

    public JwtAuthenticationFilter(JwtService jwtService, UtilisateurService utilisateurService) {
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
    }

    /**
     * Ne pas exiger de JWT sur les routes d'authentification.
     * Utilise le chemin servlet (sans context-path) pour être fiable avec
     * {@code server.servlet.context-path=/api} ou sans context-path.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
            String context = request.getContextPath();
            if (context != null && !context.isEmpty() && path.startsWith(context)) {
                path = path.substring(context.length());
                if (path.isEmpty()) {
                    path = "/";
                }
            }
        }
        return path.startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        try {
            if (!jwtService.isAccessToken(jwt)) {
                log.debug("Jeton refusé (pas un jeton d'accès): {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            final String userEmail = jwtService.extractUsername(jwt);

            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            boolean noRealUser = existing == null
                    || existing instanceof AnonymousAuthenticationToken
                    || !existing.isAuthenticated();

            if (userEmail != null && noRealUser) {
                UserDetails userDetails = this.utilisateurService.loadUserByUsername(userEmail);

                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentification JWT OK pour {}", userEmail);
                } else {
                    log.debug("Validation JWT échouée pour {}", userEmail);
                }
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalide ou illisible: {}", e.getMessage());
        } catch (UsernameNotFoundException e) {
            log.debug("Utilisateur JWT inconnu: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
