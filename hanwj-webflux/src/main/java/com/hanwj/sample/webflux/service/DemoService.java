package com.hanwj.sample.webflux.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DemoService<T> {

    boolean create(T obj);

    T getDemo(String objId);

    Mono<T> getMonoDemo(String objId);

    List<T> getAll();

    Flux<T> getFluxAll();
}
