package com.springcloud.demo.bookingsmicroservice.client.users;

import com.springcloud.demo.bookingsmicroservice.client.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users-ms", configuration = FeignConfig.class)
public interface UserClient {

    @GetMapping("/api/users/{id}")
    UserDTO findById(@PathVariable String id);
}
