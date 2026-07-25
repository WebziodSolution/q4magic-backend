package com.q4magic.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.constants.Constants;
import com.q4magic.common.dto.CustomersDto;
import com.q4magic.customers.service.CustomersService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LogManager.getLogger(JwtRequestFilter.class);

    @Autowired
    CustomersService userService;

    @Autowired
    JwtTokenUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        final String authorizationHeader = req.getHeader(Constants.REQUEST_HEADER_AUTHORIZATION);
        String requestUri = req.getRequestURI();
        log.debug("Validating request with URI : " + requestUri);

        String userName = "";
        String jwtToken = "";
        if (null != authorizationHeader && authorizationHeader.startsWith(Constants.AUTHORIZATION_BEARER)) {
            jwtToken = authorizationHeader.substring(7);
            userName = jwtUtil.extractUsername(jwtToken);

            Integer userId = jwtUtil.extractUserId(jwtToken);
            if (!StringUtils.isEmpty(userName) && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userService.loadUserByUsername(userName);
                Map<String, Object> userData = new HashMap<>();

                CustomersDto user = this.userService.getCustomerById(userId);
                if (user != null) {
                    userData.put("email", user.getEmailAddress());
                    userData.put("username", user.getUsername());
                }
                req.setAttribute("userId", userId);

                if (jwtUtil.validateToken(jwtToken, userData)) {
                    UsernamePasswordAuthenticationToken springToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    springToken.setDetails(new WebAuthenticationDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(springToken);
                } else {
                    log.error("Invalid JWT Token");
                    Map<String, Object> returnMap = new HashMap<>();
                    returnMap.put(Constants.MSG, Constants.INVALID_TOKEN);
                    returnMap.put(Constants.CODE, HttpStatus.FORBIDDEN.value());
                    returnMap.put(Constants.STATUS, Constants.STATUS_FAILURE);
                    ObjectMapper mapper = new ObjectMapper();
                    String json = mapper.writeValueAsString(returnMap);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF_8);
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.getWriter().write(json);
                    return;
                }
            }
            chain.doFilter(req, res);

        } else if (requestUri.contains("/teamDetails") || requestUri.contains("/subscriptionRates") || requestUri.contains("/calendarAppointment") || requestUri.contains("/calendarAppointmentEventType") || requestUri.contains("/teamMembers") || requestUri.contains("/teamDetails") || requestUri.contains("/opportunities/checkOpportunity") || requestUri.contains("/opportunities/createOpportunityData") || requestUri.contains("/opportunities/updateOpportunityData") || requestUri.contains("/opportunities/get/all/options") || requestUri.contains("/getCustomerByEmail") || requestUri.contains("/customerQuota") || requestUri.contains("/closeplan") || requestUri.contains("/closeplannotes") || requestUri.contains("/outlookCalendar") || requestUri.contains("/time-zones") || requestUri.contains("/dns/mx") || requestUri.contains("/api/signature") || requestUri.contains("/country") || requestUri.contains("/subUserType/create/all") || requestUri.contains("/businessInfo") || requestUri.contains("/uploadFile") || requestUri.contains("/state") || requestUri.contains("/salesforce") || requestUri.contains("/authIdDetails") || requestUri.contains("/customers") || requestUri.contains("/roles")) {
            chain.doFilter(req, res);
        } else {
            log.error("Invalid request URI : " + requestUri);

            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put(Constants.MSG, Constants.INVALID_TOKEN);
            returnMap.put(Constants.CODE, HttpStatus.FORBIDDEN.value());
            returnMap.put(Constants.STATUS, Constants.STATUS_FAILURE);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(returnMap);

            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF_8);
            res.setStatus(HttpStatus.FORBIDDEN.value());
            res.getWriter().write(json);
            return;
        }

    }
}
