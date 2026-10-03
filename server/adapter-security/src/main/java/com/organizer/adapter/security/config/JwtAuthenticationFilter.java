package com.organizer.adapter.security.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.RequiredTypeException;

import com.organizer.adapter.security.AuthenticatedUser;
import com.organizer.coreconfig.error.ValidationException;
import com.organizer.coreconfig.id.UserId;
import com.organizer.domain.user.Email;

import com.organizer.adapter.security.jwt.JwtSigner;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtSigner jwtSigner;

  public JwtAuthenticationFilter(JwtSigner jwtSigner) {
    this.jwtSigner = jwtSigner;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
      jwtSigner.verify(header.substring(7)).flatMap(JwtAuthenticationFilter::toPrincipal)
          .ifPresent(principal -> {
            var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
          });
    }

    chain.doFilter(request, response);
  }

  private static Optional<AuthenticatedUser> toPrincipal(Claims claims) {
    try {
      return Optional.of(
          new AuthenticatedUser(
              new UserId(UUID.fromString(claims.getSubject())),
              new Email(claims.get("email", String.class))));
    } catch (NullPointerException | IllegalArgumentException | ValidationException | RequiredTypeException e) {
      return Optional.empty();
    }
  }
}
