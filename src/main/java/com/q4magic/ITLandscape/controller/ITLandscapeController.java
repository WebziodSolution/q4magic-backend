package com.q4magic.ITLandscape.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/competitors")
public class ITLandscapeController {
    @Autowired
    private JwtTokenUtil jwtUtil;
}
