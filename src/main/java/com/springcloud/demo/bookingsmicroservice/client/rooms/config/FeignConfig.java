package com.springcloud.demo.bookingsmicroservice.client.rooms.config;

import com.springcloud.demo.bookingsmicroservice.monitoring.XRayFeignInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor xRayFeignInterceptor() {
        return new XRayFeignInterceptor();
    }
}
