package kz.mad.mangareader.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "novels")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Novel extends BaseEntity{
    @Column(name = "title_ru", nullable = false)
    private String titleRu;

    @Column(name = "title_en")
    private String titleEn;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image", length = 512)
    private String coverImage;

    private String author = "Неизвестен";

    @Column(nullable = false)
    private String status = "ONGOING";

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "novel_genres",
            joinColumns = @JoinColumn(name = "novel_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();
}
