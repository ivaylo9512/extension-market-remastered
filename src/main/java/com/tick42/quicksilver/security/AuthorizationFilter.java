package com.tick42.quicksilver.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthorizationFilter extends AbstractAuthenticationProcessingFilter {
    public AuthorizationFilter() {
        super("/api/**/auth/**");
        super.setAuthenticationSuccessHandler((request, response, authentication) -> {});
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse
            response) throws AuthenticationException, IOException, ServletException {
        String token = request.getHeader("Authorization");
        if(token == null || !token.startsWith("Token")){
            throw new BadCredentialsException("Jwt token is missing");
        }
        token = token.substring(6);
        return getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(token, null));
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        chain.doFilter(request, response);
    }
}