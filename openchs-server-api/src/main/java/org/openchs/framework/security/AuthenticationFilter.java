package org.openchs.framework.security;

import org.openchs.domain.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

public class AuthenticationFilter extends BasicAuthenticationFilter {
    public static final String USER_NAME_HEADER = "USER-NAME";
    public static final String AUTH_TOKEN_HEADER = "AUTH-TOKEN";
    public static final String ORGANISATION_UUID = "ORGANISATION-UUID";
    public static final String IMPLEMENTATION_COOKIE_NAME = "IMPLEMENTATION-NAME";
    private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private AuthService authService;
    private Boolean isDev;
    private String defaultUserName;

    public AuthenticationFilter(AuthenticationManager authenticationManager, AuthService authService, Boolean isDev, String defaultUserName) {
        super(authenticationManager);
        this.authService = authService;
        this.isDev = isDev;
        this.defaultUserName = defaultUserName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            String username = request.getHeader(USER_NAME_HEADER);
            String authToken = request.getHeader(AUTH_TOKEN_HEADER);
            String organisationUUID = request.getHeader(ORGANISATION_UUID);
            String method = request.getMethod();
            String requestURI = request.getRequestURI();
            String queryString = request.getQueryString();
            String implementationName = getImplementationName(request.getCookies());

            UserContext userContext = isDev
                    ? authService.authenticateByUserName(StringUtils.isEmpty(username) ? defaultUserName : username, organisationUUID, implementationName)
                    : authService.authenticateByToken(authToken, organisationUUID, implementationName);

            long start = System.currentTimeMillis();
            chain.doFilter(request, response);
            long end = System.currentTimeMillis();
            logger.info(String.format("%s %s?%s User: %s Organisation: %s Time: %s ms", method, requestURI, queryString, userContext.getUserName(), userContext.getOrganisationName(), (end - start)));
        } catch (Exception exception) {
            this.logException(request, exception);
            throw exception;
        } finally {
            UserContextHolder.clear();
        }
    }

    private String getImplementationName(Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(IMPLEMENTATION_COOKIE_NAME))
                .findFirst()
                .map(Cookie::getValue)
                .map(cookieValue -> UriUtils.decode(cookieValue, "UTF-8")).orElse(null);
    }

    private void logException(HttpServletRequest request, Exception exception) {
        logger.error("Exception on Request URI", request.getRequestURI());
        logger.error("Exception Message:", exception);
    }

}
