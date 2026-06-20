package com.duoc.bff.web;

import com.duoc.bff.domain.AuthenticatedUser;
import com.duoc.bff.domain.DashboardDto;
import com.duoc.bff.service.DashboardDtoFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** El rol y el email se derivan del JWT en AuthenticatedUser; aqui solo se delega. */
@RestController
public class DashboardController {

    private final DashboardDtoFactory factory;

    public DashboardController(DashboardDtoFactory factory) {
        this.factory = factory;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(Authentication auth) {
        var user = AuthenticatedUser.from(auth);
        return factory.create(user.role(), user.email());
    }
}
