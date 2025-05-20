package com.deepthink.org.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.deepthink.org.service.MainService;

import reactor.core.publisher.Flux;


@RestController
@CrossOrigin
@RequestMapping("/api")
public class MainController {

    @Autowired
    private MainService service;

    @PostMapping("/v1/chat")
    public String getResV1(@RequestParam("query") String prompt,
                           @RequestParam(value="mode", defaultValue="chat") String mode,
                           @RequestParam(value="model", defaultValue="qwen2.5:1.5b") String model) {
        return service.getResSimple(prompt, mode);
    }

    @PostMapping("/v2/chat")
    public Flux<String> getResV2(@RequestParam("query") String prompt,
                                 @RequestParam(value="mode", defaultValue="chat") String mode,
                                 @RequestParam(value="model", defaultValue="qwen2.5:1.5b") String model) {
        return service.getResByStream(prompt, mode, null, model);
    }

    @PostMapping("/v2/chat/media")
    public Flux<String> getResV2(@RequestParam("query") String prompt,
                                 @RequestParam(value="mode", defaultValue="chat") String mode,
                                 @RequestParam(value="file", required=false) MultipartFile file,
                                 @RequestParam(value="model", defaultValue="qwen2.5:1.5b") String model) {
        return service.getResByStream(prompt, mode, file, model);
    }
    
}