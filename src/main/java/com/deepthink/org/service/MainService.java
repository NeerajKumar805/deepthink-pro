package com.deepthink.org.service;

import org.springframework.web.multipart.MultipartFile;

import reactor.core.publisher.Flux;

public interface MainService {

    String getResSimple(String prompt, String mode);
    Flux<String> getResByStream(String prompt, String mode);
    Flux<String> getResByStream(String prompt, String mode, MultipartFile file);
    Flux<String> getResByStream(String prompt, String mode, MultipartFile file, String model);

}