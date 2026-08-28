package com.berk.urlshorten.controllers;

import com.berk.urlshorten.domain.dto.UrlDto;
import com.berk.urlshorten.domain.entities.UrlEntity;
import com.berk.urlshorten.sevices.UrlShortenService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    //make sure to return 201 created

    @GetMapping(path = "/shorten")
    public Page<UrlDto> listUrl(Pageable pageable){
        Page<UrlEntity> urls = urlShortenService.findAll(pageable);
        return urls.map(urlEntity ->modelMapper.map(urlEntity, UrlDto.class));
    }
    //400 bad request

    @GetMapping(path = "/shorten/{shortUrl}")
    public ResponseEntity<UrlDto> listById(@PathVariable("shortUrl") String shortUrl){
            return urlShortenService.findOne(shortUrl)
                .map(urlEntity -> {
                    UrlDto urlDto =  modelMapper.map(urlEntity, UrlDto.class);
                    return new ResponseEntity<>(urlDto, HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found with shortUrl: " + shortUrl));
        }

    @PutMapping(path = "/shorten/{shortUrl}")
    public ResponseEntity<UrlDto> updateByShortCode(@PathVariable("shortUrl") String shortUrl, @RequestBody UrlDto urlDto){

        UrlEntity urlEntity = modelMapper.map(urlDto, UrlEntity.class);
        UrlEntity updatedEntity = urlShortenService.partialUpdate(shortUrl, urlEntity);
        return new ResponseEntity<>(modelMapper.map(updatedEntity, UrlDto.class), HttpStatus.OK);
    }

    @DeleteMapping(path = "/shorten/{shortUrl}")
    public ResponseEntity<Void> deleteUrl(@PathVariable("shortUrl") String shortUrl){
        urlShortenService.delete(shortUrl);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //one more of GET /shorten/abc123/stats
}
