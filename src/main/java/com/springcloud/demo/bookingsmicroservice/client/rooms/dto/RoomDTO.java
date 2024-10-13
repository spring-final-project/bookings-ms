package com.springcloud.demo.bookingsmicroservice.client.rooms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private UUID id;
    private Integer num;
    private String name;
    private Integer floor;
    private Integer maxCapacity;
    private String description;
    private String ownerId;
    private List<String> images;
    private Integer simpleBeds;
    private Integer mediumBeds;
    private Integer doubleBeds;
}
