package com.elias.aiproxy.service;

import reactor.core.publisher.Flux;

public interface TaskAiService {

    Flux<String> chat(String question);
}
