package com.springcloud.demo.bookingsmicroservice.client.rooms;

import com.springcloud.demo.bookingsmicroservice.client.config.FeignConfig;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rooms-ms", configuration = FeignConfig.class)
public interface RoomClient {

    @GetMapping("/api/rooms/{id}")
    RoomDTO findById(@PathVariable String id);
}
