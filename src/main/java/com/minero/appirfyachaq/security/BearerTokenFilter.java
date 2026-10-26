package com.minero.appirfyachaq.security;

import com.minero.appirfyachaq.repository.RepositoryUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    private final TokenService tokens;
    private final RepositoryUser users;

    public BearerTokenFilter(TokenService tokens, RepositoryUser users) {
        this.tokens = tokens;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String id = tokens.validateAndGetUserId(header.substring(7));
            if (id != null) {
                users.findById(id).filter(user -> Boolean.TRUE.equals(user.getStatus())).ifPresent(user -> {
                    boolean admin = "admin".equalsIgnoreCase(user.getRole());
                    var authorities = List.of(new SimpleGrantedAuthority(admin ? "ROLE_ADMIN" : "ROLE_MINERO"));
                    var auth = new UsernamePasswordAuthenticationToken(user.getIdUser(), null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
