package com.berk.urlshorten.sevices;

import com.berk.urlshorten.domain.entities.UrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UrlShortenService {

    UrlEntity save(UrlEntity urlEntity);

    Page<UrlEntity> findAll(Pageable pageable);

    Optional<UrlEntity> findOne(String ShortUrl);

    UrlEntity partialUpdate(String shortUrl, UrlEntity urlEntity);

    void delete(String shortUrl);

    Boolean isExist(Long id);
}
