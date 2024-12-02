package com.springcloud.demo.bookingsmicroservice.client.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.bookingsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.bookingsmicroservice.exceptions.InheritedException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserClientImpl implements UserClient {

    private final UserClient userClient;

    @Override
    @CircuitBreaker(name = "users-service", fallbackMethod = "findUserByIdFallback")
    public UserDTO findById(String id) {
        return userClient.findById(id);
    }

    public UserDTO findUserByIdFallback(String id, Throwable e) throws Exception {
        if(!(e instanceof FeignException.FeignClientException feignClientException)){
            throw new ForbiddenException("Users service not available. Try later");
        }

        Map body = new ObjectMapper().readValue(feignClientException.contentUTF8(), Map.class);

        throw new InheritedException(
                feignClientException.status(),
                (String) body.get("message")
        );
    }
}
