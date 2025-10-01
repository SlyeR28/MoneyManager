package org.moneymanagement.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")

public class HomeController {

    @GetMapping("/home")
    public String healthCheck(){
        return "Application is Running";
    }


    @GetMapping("/csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        // Spring Security automatically attaches the CsrfToken object to the request
        return (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    }
}
