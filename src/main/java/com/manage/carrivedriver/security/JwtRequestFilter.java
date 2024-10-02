package com.manage.carrivedriver.security;

import com.manage.carrive.entity.Driver;
import com.manage.carriveutility.repository.DriverRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Autowired
    private DriverRepository driverRepository;

    public static Driver driver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        logger.info("request.getRequestURL(): "+request.getRequestURL());
        logger.info("request.getRequestURI(): "+request.getRequestURI());
        logger.info("headers contains in request are: ");
        Enumeration<String> listHeaders = request.getHeaderNames();
        do {
            String currentHeader = listHeaders.nextElement();
            logger.info("\t........: "+currentHeader+" = "+request.getHeader(currentHeader));
        }while(listHeaders.hasMoreElements());

        final String requestTokenHeader = request.getHeader("Authorization");

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(jwtToken)
                        .getBody();
                String username = claims.getSubject();
                driver = driverRepository.findByEmail(username).orElse(null);
                // Configurez l'authentification ici si nécessaire
            } catch (Exception e) {
                // Gérer l'exception
            }
        }
        chain.doFilter(request, response);
    }
}
