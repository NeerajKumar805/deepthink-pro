package com.deepthink.org.service;

import reactor.core.publisher.Flux;

public interface DeepthinkSimpleService {

	Flux<String> getResponseByStream(String prompt);

}
