package com.organizer.adapter.security.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.util.List;

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
    if (header != null && header.startsWith("Bearer ")) {
      jwtSigner.verify(header.substring(7)).ifPresent(claims -> {
        var authentication = new UsernamePasswordAuthenticationToken(
            claims.getSubject(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      });
    }

    chain.doFilter(request, response);
  }
}
