package com.piotr.matrix.gateway.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Log4j2
@Configuration
@Order(-1)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBuffer buffer;
        if (ex instanceof GatewayException gatewayException) {
            var status = gatewayException.getCode();
            var message = gatewayException.getMessage();
            log.error("Gateway Exception Status:{}, Message: {}", status, message);
            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(status));
            exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            String responseBody = String.format(
                    "{\"status\":%d, \"message\":\"%s\", \"stack trace\":\"%s\"}",
                    status,
                    message,
                    gatewayException.getStackTrace()[0]
            );
            buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            String responseBody = "Other error occurred";
            buffer = exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
            log.error("Exception happened: {}", buffer.toString(StandardCharsets.UTF_8));
        }
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
