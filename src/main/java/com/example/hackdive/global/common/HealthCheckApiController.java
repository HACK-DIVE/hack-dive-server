package com.example.hackdive.global.common;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class HealthCheckApiController {

    @RequestMapping("/")
    public String hackdiveServer() {
        return "hackdiveServer";

    }

    @RequestMapping("/example")
    public ResponseEntity<SuccessResponse<?>> example() {
        return SuccessResponse.ok("example");
    }
}