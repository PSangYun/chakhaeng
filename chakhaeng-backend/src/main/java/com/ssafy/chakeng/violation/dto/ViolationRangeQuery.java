package com.ssafy.chakeng.violation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ViolationRangeQuery {
    @NotNull
    private OffsetDateTime from;
    @NotNull
    private OffsetDateTime to;
    private UUID videoId;
}