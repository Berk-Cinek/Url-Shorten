package com.berk.urlshorten.sevices.impl;

import com.berk.urlshorten.domain.entities.UrlEntity;
import com.berk.urlshorten.repository.UrlRepository;
import com.berk.urlshorten.sevices.UrlShortenService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class UrlShortenImpl implements UrlShortenService {

    private final UrlRepository urlRepository;

    public UrlShortenImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    @CachePut(value = "URL_CACHE", key = "#result.getShortURL()")
    public UrlEntity save(UrlEntity urlEntity) {
        urlEntity.setAccessCount(0);
        return urlRepository.save(urlEntity);
    }

    @Override
    public Page<UrlEntity> findAll(Pageable pageable) {
        return urlRepository.findAll(pageable);
    }

    @Override
    @Cacheable(value = "URL_CACHE", key = "#shortUrl", unless = "#result == null")
    public Optional<UrlEntity> findOne(String shortUrl) {
        return urlRepository.findByShortURL(shortUrl);
    }

    @Override
    @CachePut(value = "URL_CACHE", key = "#result.getShortURL()")
    public UrlEntity partialUpdate(String shortUrl, UrlEntity urlEntity) {

        return urlRepository.findByShortURL(shortUrl).map(existingUrlEntity ->{
                Optional.ofNullable(urlEntity.getUrl()).ifPresent((existingUrlEntity::setUrl));
                Optional.ofNullable(urlEntity.getShortURL()).ifPresent((existingUrlEntity::setShortURL));
                existingUrlEntity.setAccessCount(existingUrlEntity.getAccessCount() + 1);
                return urlRepository.save((existingUrlEntity));
                }
        ).orElseThrow(() -> new RuntimeException("record not found to update"));
    }

    @Override
    @CacheEvict(value = "URL_CACHE", key = "#shortUrl")
    public void delete(String shortUrl) {
        UrlEntity entity = urlRepository.findByShortURL(shortUrl)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "cannot find entry with shortUrl:" + shortUrl));

        urlRepository.deleteById(entity.getId());
    }

    @Override
    public Boolean isExist(Long id) {
        return urlRepository.existsById(id);
    }


}
