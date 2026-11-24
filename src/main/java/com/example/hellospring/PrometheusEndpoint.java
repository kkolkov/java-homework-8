package com.example.hellospring;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.stereotype.Component;

@Component
@WebEndpoint(id = "prometheus")
public class PrometheusEndpoint {
    private final PrometheusMeterRegistry registry;

    public PrometheusEndpoint(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @ReadOperation(produces = "text/plain;version=0.0.4;charset=utf-8")
    public String scrape() {
        return registry.scrape();
    }
}
