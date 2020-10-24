package com.tick42.quicksilver.config;

import com.tick42.quicksilver.security.AuthenticationFilter;
import com.tick42.quicksilver.security.AuthorizationFilter;
import com.tick42.quicksilver.security.AuthorizationProvider;
import com.tick42.quicksilver.security.FailureHandler;
import com.tick42.quicksilver.services.UserServiceImpl;
import org.apache.http.HttpHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Collections;

@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserServiceImpl userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationProvider authorizationProvider;

    public SecurityConfig(UserServiceImpl userService, PasswordEncoder passwordEncoder, AuthorizationProvider authorizationProvider) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authorizationProvider = authorizationProvider;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }
    @Bean
    public AuthenticationManager authenticationManagerAuthorization() {
        return new ProviderManager(Collections.singletonList(authorizationProvider));
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");  // TODO: lock down before deploying
        config.addAllowedHeader("*");
        config.addExposedHeader(HttpHeaders.AUTHORIZATION);
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable().authorizeRequests()
                .antMatchers("**/api/**/auth/**").authenticated()
                .and()
                .addFilterBefore(authenticationFilter(), ConcurrentSessionFilter.class)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.headers().cacheControl();
    }

    private AuthorizationFilter authorizationFilter() {
        AuthorizationFilter filter = new AuthorizationFilter();
        filter.setFilterProcessesUrl("/api/**/auth/**");
        filter.setAuthenticationManager(authenticationManagerAuthorization());
        filter.setAuthenticationFailureHandler(new FailureHandler());
        filter.setAuthenticationSuccessHandler((request, response, authentication) -> {});
        return filter;
    }

    private AuthenticationFilter authenticationFilter() throws Exception{
        final AuthenticationFilter authenticationFilter = new AuthenticationFilter();
        authenticationFilter.setFilterProcessesUrl("/api/users/login");
        authenticationFilter.setAuthenticationFailureHandler(new FailureHandler());
        authenticationFilter.setAuthenticationManager(authenticationManager());
        return authenticationFilter;
    }

}
