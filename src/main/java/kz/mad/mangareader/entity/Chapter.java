package kz.mad.mangareader.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "chapters")
@Setter
@Getter
public class Chapter extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;

    @Column(name="tome_number", nullable = false)
    private Integer tomeNumber = 1;

    @Column(name="chapter_number", nullable = false)
    private BigDecimal chapterNumber;

    private String title;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name="created_at", nullable = false, insertable = false,updatable = false)
    private OffsetDateTime createdAt;
}
