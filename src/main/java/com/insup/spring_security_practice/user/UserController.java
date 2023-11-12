package com.insup.spring_security_practice.user;

import com.insup.spring_security_practice.config.auth.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final OAuthService oAuthService;

}
