//package com.medvault.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtUtil                jwtUtil;
//    private final UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest  request,
//                                    HttpServletResponse response,
//                                    FilterChain         filterChain)
//            throws ServletException, IOException {
//
//        // Skip filter for OPTIONS pre-flight requests
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            String token = extractToken(request);
//
//            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                String email = jwtUtil.extractEmail(token);
//
//                if (email != null) {
//                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
//
//                    if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
//                        UsernamePasswordAuthenticationToken authToken =
//                                new UsernamePasswordAuthenticationToken(
//                                        userDetails, null, userDetails.getAuthorities());
//                        authToken.setDetails(
//                                new WebAuthenticationDetailsSource().buildDetails(request));
//                        SecurityContextHolder.getContext().setAuthentication(authToken);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            // Invalid / expired token — clear context and let Spring Security handle it
//            log.warn("JWT authentication failed for [{}]: {}", request.getRequestURI(), e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            return header.substring(7);
//        }
//        return null;
//    }
//}


//package com.medvault.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtUtil                jwtUtil;
//    private final UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest  request,
//                                    HttpServletResponse response,
//                                    FilterChain         filterChain)
//            throws ServletException, IOException {
//
//        // ✅ Skip OPTIONS preflight immediately
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            // ✅ Check X-Auth-User header set by Gateway
//            //    Gateway already validated the JWT — trust it directly
//            String authUser = request.getHeader("X-Auth-User");
//            if (StringUtils.hasText(authUser) &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                log.debug("Pre-authenticated by Gateway: {}", authUser);
//                UserDetails userDetails =
//                    userDetailsService.loadUserByUsername(authUser);
//
//                UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                authToken.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                filterChain.doFilter(request, response);
//                return;
//            }
//            
//
//            // ✅ Fallback: validate JWT directly (dev / direct calls)
//            String token = extractToken(request);
//
//            if (token != null &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                String email = jwtUtil.extractEmail(token);
//
//                if (email != null) {
//                    UserDetails userDetails =
//                        userDetailsService.loadUserByUsername(email);
//
//                    if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
//                        UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//                        authToken.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request));
//                        SecurityContextHolder.getContext().setAuthentication(authToken);
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            log.warn("JWT authentication failed for [{}]: {}",
//                request.getRequestURI(), e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(HttpServletRequest request) {
//        String header = request.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            return header.substring(7);
//        }
//        return null;
//    }
//}
//package com.medvault.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtUtil                jwtUtil;
//    private final UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest  request,
//                                    HttpServletResponse response,
//                                    FilterChain         filterChain)
//            throws ServletException, IOException {
//
//        // ── Skip OPTIONS preflight immediately ────────────────────────────
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            // ── 1. Gateway pre-auth: X-Auth-User header ───────────────────
//            // Gateway already validated the JWT — trust it directly
//            String authUser = request.getHeader("X-Auth-User");
//            if (StringUtils.hasText(authUser) &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                log.debug("Pre-authenticated by Gateway: {}", authUser);
//
//                UserDetails userDetails =
//                    userDetailsService.loadUserByUsername(authUser);
//
//                UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                authToken.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request));
//
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // ── 2. Direct JWT validation (dev / direct calls / SSE) ───────
//            String token = extractToken(request);
//
//            if (token != null &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                String email = jwtUtil.extractEmail(token);
//
//                if (email != null) {
//                    UserDetails userDetails =
//                        userDetailsService.loadUserByUsername(email);
//
//                    if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
//                        UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//                        authToken.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                        log.debug("JWT authenticated: {} [{}]",
//                            email, request.getRequestURI());
//                    } else {
//                        log.warn("Invalid JWT for email={} uri={}",
//                            email, request.getRequestURI());
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            log.warn("JWT authentication failed for [{}]: {}",
//                request.getRequestURI(), e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    // ─────────────────────────────────────────────────────────────────────
//    // Token extraction — header first, then query param (SSE)
//    // ─────────────────────────────────────────────────────────────────────
//    private String extractToken(HttpServletRequest request) {
//
//        // 1. Authorization: Bearer <token>  — all normal API calls
//        String header = request.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            return header.substring(7);
//        }
//
//        // 2. ?token=<token>  — SSE only (EventSource cannot set headers)
//        String queryToken = request.getParameter("token");
//        if (StringUtils.hasText(queryToken)) {
//            log.debug("JWT extracted from query param for SSE: {}",
//                request.getRequestURI());
//            return queryToken;
//        }
//
//        return null;
//    }
//}
//package com.medvault.security;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JwtAuthFilter extends OncePerRequestFilter {
//
//    private final JwtUtil                jwtUtil;
//    private final UserDetailsServiceImpl userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest  request,
//                                    HttpServletResponse response,
//                                    FilterChain         filterChain)
//            throws ServletException, IOException {
//
//        // ── Skip OPTIONS preflight ────────────────────────────────────────
//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // ── FIX 3: Debug log — shows exactly what arrives ─────────────────
//        log.debug("URI={} | X-Auth-User={} | Auth-Header={}",
//            request.getRequestURI(),
//            request.getHeader("X-Auth-User"),
//            request.getHeader("Authorization") != null ? "present" : "missing");
//
//        try {
//            // ── 1. Gateway pre-auth: X-Auth-User header ───────────────────
//            String authUser = request.getHeader("X-Auth-User");
//
//            if (StringUtils.hasText(authUser) &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                log.debug("Pre-authenticated by Gateway: {}", authUser);
//
//                UserDetails userDetails =
//                    userDetailsService.loadUserByUsername(authUser);
//
//                UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//
//                authToken.setDetails(
//                    new WebAuthenticationDetailsSource().buildDetails(request));
//
//                SecurityContextHolder.getContext().setAuthentication(authToken);
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // ── 2. Direct JWT validation ───────────────────────────────────
//            String token = extractToken(request);
//
//            if (token != null &&
//                SecurityContextHolder.getContext().getAuthentication() == null) {
//
//                String email = jwtUtil.extractEmail(token);
//
//                if (email != null) {
//                    UserDetails userDetails =
//                        userDetailsService.loadUserByUsername(email);
//
//                    if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {
//
//                        UsernamePasswordAuthenticationToken authToken =
//                            new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//
//                        authToken.setDetails(
//                            new WebAuthenticationDetailsSource().buildDetails(request));
//
//                        SecurityContextHolder.getContext().setAuthentication(authToken);
//
//                        log.debug("JWT authenticated: {} [{}]",
//                            email, request.getRequestURI());
//
//                    } else {
//                        // ── FIX 4: Log token expiry clearly ───────────────
//                        log.warn("Invalid or expired JWT for email={} uri={}",
//                            email, request.getRequestURI());
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            log.warn("JWT authentication failed for [{}]: {}",
//                request.getRequestURI(), e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private String extractToken(HttpServletRequest request) {
//
//        // 1. Authorization: Bearer <token>
//        String header = request.getHeader("Authorization");
//        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
//            return header.substring(7);
//        }
//
//        // 2. ?token=<token>  — SSE only
//        String queryToken = request.getParameter("token");
//        if (StringUtils.hasText(queryToken)) {
//            log.debug("JWT extracted from query param for SSE: {}",
//                request.getRequestURI());
//            return queryToken;
//        }
//
//        return null;
//    }
//}
package com.medvault.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil                jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * SSE (SseEmitter) and other async responses trigger a Tomcat internal
     * async re-dispatch on a new thread. OncePerRequestFilter would silently
     * skip the body anyway via its already-filtered attribute, but overriding
     * this explicitly ensures the filter is bypassed cleanly for every async
     * dispatch without relying on the attribute being carried over.
     */
    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        // ── Skip OPTIONS preflight ────────────────────────────────────────
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("URI={} | X-Auth-User={} | Auth-Header={}",
            request.getRequestURI(),
            request.getHeader("X-Auth-User"),
            request.getHeader("Authorization") != null ? "present" : "missing");

        try {
            // ── 1. Gateway pre-auth: X-Auth-User header ───────────────────
            String authUser = request.getHeader("X-Auth-User");

            if (StringUtils.hasText(authUser) &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                log.debug("Pre-authenticated by Gateway: {}", authUser);

                UserDetails userDetails =
                    userDetailsService.loadUserByUsername(authUser);

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
                return;
            }

            // ── 2. Direct JWT validation ───────────────────────────────────
            String token = extractToken(request);

            if (token != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                String email = jwtUtil.extractEmail(token);

                if (email != null) {
                    UserDetails userDetails =
                        userDetailsService.loadUserByUsername(email);

                    if (jwtUtil.isTokenValid(token, userDetails.getUsername())) {

                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("JWT authenticated: {} [{}]",
                            email, request.getRequestURI());

                    } else {
                        log.warn("Invalid or expired JWT for email={} uri={}",
                            email, request.getRequestURI());
                    }
                }
            }

        } catch (Exception e) {
            log.warn("JWT authentication failed for [{}]: {}",
                request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {

        // 1. Authorization: Bearer <token>
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 2. ?token=<token>  — SSE only (EventSource cannot set headers)
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            log.debug("JWT extracted from query param for SSE: {}",
                request.getRequestURI());
            return queryToken;
        }

        return null;
    }
}