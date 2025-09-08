package com.ssafy.chakeng.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ReportCreateRequest {
    @NotNull
    private UUID ownerId;
    @NotBlank
    private String violationType;
    @NotBlank
    private String location;
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String plateNumber;
    @NotBlank
    private String date;
    @NotBlank
    private String time;
}
