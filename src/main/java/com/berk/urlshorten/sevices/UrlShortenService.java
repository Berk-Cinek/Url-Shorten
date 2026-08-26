package com.berk.urlshorten.sevices;

import com.berk.urlshorten.domain.entities.UrlEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UrlShortenService {

    UrlEntity save(UrlEntity urlEntity);

    Page<UrlEntity> findAll(Pageable pageable);

    Optional<UrlEntity> findOne(Long id);

    UrlEntity partialUpdate(Long id, UrlEntity urlEntity);

    void delete(Long id);

    Boolean isExist(Long id);
}
