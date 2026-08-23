package com.berk.urlshorten.controllers;

import com.berk.urlshorten.domain.dto.UrlDto;
import com.berk.urlshorten.domain.entities.UrlEntity;
import com.berk.urlshorten.sevices.UrlShortenService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlController {
    private UrlShortenService urlShortenService;
    private ModelMapper modelMapper = new ModelMapper();

    public UrlController(UrlShortenService urlShortenService) {
        this.urlShortenService = urlShortenService;
    }

    @PostMapping(path = "/shorten")
    public ResponseEntity<UrlDto> createUrl(@RequestBody UrlDto urlDto){
        UrlEntity urlEntity = urlShortenService.save(modelMapper.map(urlDto, UrlEntity.class));
        return new ResponseEntity<>(modelMapper.map(urlEntity, UrlDto.class), HttpStatus.CREATED);
    }

    @GetMapping(path = "/shorten")
    public Page<UrlDto> listUrl(Pageable pageable){
        Page<UrlEntity> urls = urlShortenService.findAll(pageable);
        return urls.map(_urlEntity ->modelMapper.map(urls, UrlDto.class));
    }
}
