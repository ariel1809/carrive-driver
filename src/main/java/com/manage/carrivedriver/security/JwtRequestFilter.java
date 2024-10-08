package com.manage.carrivedriver.security;

import com.manage.carrive.entity.Driver;
import com.manage.carriveutility.repository.DriverRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
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

        logger.info("Request URL: " + request.getRequestURL());
        logger.info("Request URI: " + request.getRequestURI());

        // Log all headers
        logger.info("Headers in request:");
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            logger.info(headerName + ": " + request.getHeader(headerName));
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey)
                        .parseClaimsJws(jwtToken)
                        .getBody();
                username = claims.getSubject(); // Retrieve email or username from the token
            } catch (ExpiredJwtException e) {
                logger.error("JWT token is expired", e);
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token", e);
            } catch (SignatureException e) {
                logger.error("Invalid JWT signature", e);
            } catch (Exception e) {
                logger.error("JWT token processing failed", e);
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");
        }

        // Once we get the token, validate it and set the Spring Security context
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the user (Driver) based on email/username from the token
            driver = driverRepository.findByEmail(username).orElse(null);

            if (driver != null) {
                // In a real application, you'd implement a DriverDetailsService similar to UserDetailsService
                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                        driver.getEmail(), "", new ArrayList<>()); // Replace with actual authorities if applicable

                // Create authentication token for Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue the filter chain
        chain.doFilter(request, response);
    }
}
