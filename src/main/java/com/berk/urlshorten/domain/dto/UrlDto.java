package com.berk.urlshorten.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UrlDto {

    private Long id;

    private String url;

    private String shortURL;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer accessCount;
}
