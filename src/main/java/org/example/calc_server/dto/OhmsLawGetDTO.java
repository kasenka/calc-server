package org.example.calc_server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OhmsLawGetDTO {
    private Double voltage;
    private Double current;
    private Double resistance;
}
