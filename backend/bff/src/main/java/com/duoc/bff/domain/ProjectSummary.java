package com.duoc.bff.domain;

import java.time.LocalDate;

public record ProjectSummary(
        Long id,
        String name,
        String description,
        String status,
        LocalDate startDate,
        LocalDate plannedEndDate,
        String ownerId
) {}
