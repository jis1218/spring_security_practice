package com.insup.spring_security_practice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .authorizeHttpRequests((auth) ->
                auth.requestMatchers("/user").hasRole("USER")
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


}
