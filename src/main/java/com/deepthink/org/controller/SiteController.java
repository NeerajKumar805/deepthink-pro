package com.deepthink.org.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deepthink.org.dto.SiteRequest;
import com.deepthink.org.dto.SiteResponse;
import com.deepthink.org.service.SiteGeneratorService;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class SiteController {

    private final SiteGeneratorService generator;

    public SiteController(SiteGeneratorService generator) {
        this.generator = generator;
    }

    /** Generate the site (HTML/CSS/JS + ZIP) */
    @PostMapping("/generate")
    public SiteResponse generate(@RequestBody SiteRequest request) {
        return generator.generateSite(request);
    }

    /** Serve the ZIP for download */
    @GetMapping("/projects/{file:.+\\.zip}")
    public ResponseEntity<Resource> download(@PathVariable String file) {
        Resource res = generator.loadZipAsResource(file);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(res);
    }
}
