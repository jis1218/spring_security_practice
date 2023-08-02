package com.insup.spring_security_practice.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .authorizeHttpRequests((auth) ->
                auth.requestMatchers("/login").permitAll()
                    .requestMatchers("/user").hasRole("USER")
                    .requestMatchers("/admin/pay").hasRole("ADMIN")
                    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SYS")
                    .anyRequest().authenticated()); // 어떠한 요청도 인증을 받아야 함

        httpSecurity.formLogin(httpSecurityFormLoginConfigurer ->
            httpSecurityFormLoginConfigurer.loginProcessingUrl("/login")
                .usernameParameter("username"));

        httpSecurity.logout(httpSecurityLogoutConfigurer -> httpSecurityLogoutConfigurer.logoutUrl("/logout").logoutSuccessUrl("/login").addLogoutHandler(
            new LogoutHandler() {
                @Override
                public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                    request.getSession().invalidate();
                }
            })
        );

//        httpSecurity.rememberMe(httpSecurityRememberMeConfigurer -> httpSecurityRememberMeConfigurer
//            .alwaysRemember(true)
//            .userDetailsService(userDetailsService));

        httpSecurity.sessionManagement(httpSecuritySessionManagementConfigurer -> {
            httpSecuritySessionManagementConfigurer.maximumSessions(1) // -1 : 무제한 로그인
                .maxSessionsPreventsLogin(false) // 동시 로그인 차단함, false : 기존 세션 만료(default)
                .expiredUrl("/expired"); // 세션이 만료된 경우 이동할 페이지


        });

        httpSecurity.exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
            httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new AuthenticationEntryPoint() {
                @Override
                public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
                    throws IOException, ServletException {
                    response.sendRedirect("/login");
                }
            })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
                        throws IOException, ServletException {
                        response.sendRedirect("/denied");
                    }
                })
        );

        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
            .username("user")
            .password("{noop}1111")
            .roles("USER")
            .build();
        UserDetails user2 = User.builder()
            .username("sys")
            .password("{noop}1111")
            .roles("SYS")
            .build();
        UserDetails user3 = User.builder()
            .username("admin")
            .password("{noop}1111")
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user, user2, user3);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
                throws IOException, ServletException {
                RequestCache requestCache = new HttpSessionRequestCache();
                SavedRequest savedRequest = requestCache.getRequest(request, response);
                String redirectUrl = savedRequest.getRedirectUrl(); // 세션에 저장되어 있던 인증 정보로 redirect
                response.sendRedirect(redirectUrl);
            }
        };
    }
}
