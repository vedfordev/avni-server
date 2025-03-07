package org.avni.server.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {
    public static final String LOGOUT_URL = "/web/logout";

    @RequestMapping(value = LOGOUT_URL, method = RequestMethod.GET)
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        return "Success";
    }
}
