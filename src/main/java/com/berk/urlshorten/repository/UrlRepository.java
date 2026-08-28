package com.berk.urlshorten.repository;

import com.berk.urlshorten.domain.entities.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    boolean existsById(Long id);

    Optional<UrlEntity> findByShortURL(String shortURL);
}
