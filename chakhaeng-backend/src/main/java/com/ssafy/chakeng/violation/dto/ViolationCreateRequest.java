package com.ssafy.chakeng.violation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ViolationCreateRequest {
    @NotNull
    private UUID videoId;
    @NotBlank
    private String type;
    private String plate;
    private String locationText;
    private OffsetDateTime occurredAt;
}