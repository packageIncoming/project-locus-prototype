package com.locus.projectlocusprototype.Auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // check if there is a jwt token present
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !jwt.startsWith("Bearer ")) {
            // If there isn't a token present then do nothing
            filterChain.doFilter(request, response);
            return;
        }
        // Parse the JWT
        jwt = jwt.substring(7); // remove "Bearer "
        String username = jwtService.extractUsername(jwt);

        // If the user is not already authenticated...
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Get the user's details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate the JWT against the UserDetails
            if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {

                // JWT is valid, create an authentication token
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No credentials needed after auth
                        userDetails.getAuthorities()
                );

                // Add details from request (IP, Session ID, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Save the authentication token to the current security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // JWT is INVALID, throw an exception
                throw new BadCredentialsException("ERROR: Invalid JWT");
            }
        }

        // Continue with other filters in the chain
        filterChain.doFilter(request, response);
    }
}
