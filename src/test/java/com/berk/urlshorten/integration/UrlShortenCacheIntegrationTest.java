package com.berk.urlshorten.integration;

import com.berk.urlshorten.domain.entities.UrlEntity;
import com.berk.urlshorten.repository.UrlRepository;
import com.berk.urlshorten.sevices.UrlShortenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public class UrlShortenCacheIntegrationTest {

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UrlShortenService urlShortenService;

    @Autowired
    private UrlRepository urlRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void findOneCachePopulate(){
        UrlEntity saved = urlRepository.save(UrlEntity.builder()
                        .url("https://example.com")
                        .build());

        urlShortenService.findOne(saved.getId());

        Cache cache = cacheManager.getCache("URL_CACHE");
        assert cache != null;
        Cache.ValueWrapper cached = cache.get(saved.getId());

        assertThat(cached).isNotNull();
    }

    @Test
    void findOne_SecondCall_PopulatesCache(){
        UrlEntity saved = urlRepository.save(UrlEntity.builder()
                        .url("https://example.com")
                        .build());

        urlShortenService.findOne(saved.getId());
        urlRepository.deleteById(saved.getId());

        Optional<UrlEntity> secondCall = urlShortenService.findOne(saved.getId());

        assertThat(secondCall).isPresent();
    }

    @Test
    void delete_emptiesCache() {
        UrlEntity saved = urlRepository.save(UrlEntity.builder()
                .url("https://example.com")
                .build());

        urlShortenService.findOne(saved.getId()); // explicitly populate cache

        urlShortenService.delete(saved.getId());

        Optional<UrlEntity> secondCall = urlShortenService.findOne(saved.getId());

        assertThat(secondCall).isNotPresent();
    }

    @Test
    void save_PopulatesCache(){
        UrlEntity saved = urlShortenService.save(UrlEntity.builder()
                .url("https://example.com")
                .build());

        Optional<UrlEntity> call = urlShortenService.findOne(saved.getId());

        assertThat(call).isPresent();
    }

    @Test
    void save_OverWriteCache(){
        UrlEntity saved = urlShortenService.save(UrlEntity.builder()
                .url("https://example.com")
                .build());

        UrlEntity update =UrlEntity.builder()
                .url("https://Student.com")
                .build();

        urlShortenService.partialUpdate(saved.getId(), update);

        Cache cache = cacheManager.getCache("URL_CACHE");
        assert cache != null;
        Cache.ValueWrapper cached = cache.get(saved.getId());

        assertThat(cached).isNotNull();

        UrlEntity cachedEntity = (UrlEntity) cached.get();
        assert cachedEntity != null;
        assertThat(cachedEntity.getUrl()).isEqualTo("https://Student.com");

    }

}

