package com.berk.urlshorten.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import io.seruco.encoding.base62.Base62;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url_entity")
public class UrlEntity implements Serializable {

    private static final Base62 BASE62 = Base62.createInstance();

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "url_seq_gen")
    @SequenceGenerator(
            name = "url_seq_gen",
            sequenceName = "url_sequence",
            allocationSize = 10
    )
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false, name = "short_url")
    private String shortURL;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "access_count")
    private Integer accessCount;

    @PrePersist
    private void assignShortCode(){
        if (this.shortURL == null){
            byte[] idBytes = Long.toString(id).getBytes();
            this.setShortURL(new String(BASE62.encode(idBytes)));
        }
    }
}


