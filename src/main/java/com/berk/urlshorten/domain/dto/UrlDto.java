package com.berk.urlshorten.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data

public class UrlDto {

    private Long id;

    private String url;

    private String shortURL;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer accessCount;
}
