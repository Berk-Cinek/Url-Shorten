package com.berk.urlshorten.repository;

import com.berk.urlshorten.domain.entities.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    boolean existsByUrlEntity_Id(Long UrlEntityId);
}
