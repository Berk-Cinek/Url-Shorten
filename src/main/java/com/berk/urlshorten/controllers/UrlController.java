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

    @GetMapping(path = "/shorten/{id}")
    public ResponseEntity<UrlDto> listById(@PathVariable("id") Long id){
            return urlShortenService.findOne(id)
                .map(urlEntity -> {
                    UrlDto urlDto =  modelMapper.map(urlEntity, UrlDto.class);
                    return new ResponseEntity<>(urlDto, HttpStatus.OK);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Url not found with id: " + id));
        }

    @PutMapping(path = "/shorten/{id}")
    public ResponseEntity<UrlDto> updateById(@PathVariable("id") Long id, @RequestBody UrlDto urlDto){

        UrlEntity urlEntity = modelMapper.map(urlDto, UrlEntity.class);
        UrlEntity updatedEntity = urlShortenService.partialUpdate(id, urlEntity);
        return new ResponseEntity<>(modelMapper.map(updatedEntity, UrlDto.class), HttpStatus.OK);
    }

    @DeleteMapping(path = "/shorten/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable("id") Long id){
        urlShortenService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //one more of GET /shorten/abc123/stats
}
