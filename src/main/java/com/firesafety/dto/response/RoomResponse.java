package com.firesafety.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private Long id;
    private String number;
    private Integer floor;
    private String purpose;
    private Long buildingId;
    private String buildingName;
    private int sensorCount;
}
