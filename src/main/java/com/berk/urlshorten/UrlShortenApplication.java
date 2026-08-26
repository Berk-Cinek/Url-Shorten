package com.berk.urlshorten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class UrlShortenApplication {

    static void main(String[] args) {
        SpringApplication.run(UrlShortenApplication.class, args);
    }

}
