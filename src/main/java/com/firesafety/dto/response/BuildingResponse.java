package com.firesafety.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuildingResponse {
    private Long id;
    private String name;
    private String address;
    private String description;
    private int roomCount;
}
