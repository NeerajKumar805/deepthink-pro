package com.deepthink.org.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deepthink.org.service.DeepthinkSimpleService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/apii")
@CrossOrigin
public class DeepthinkSimpleController {

	@Autowired
	private DeepthinkSimpleService service;
	
	@PostMapping("/v2/chat")
	public Flux<String> getResponseByStream(@RequestParam(value = "prompt", defaultValue = "Hello", required = false) String param) {
		return service.getResponseByStream(param);
	}
	
}
