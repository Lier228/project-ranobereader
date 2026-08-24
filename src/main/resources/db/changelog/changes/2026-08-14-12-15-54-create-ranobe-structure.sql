--liquibase formatted sql

--changeset ranobereader:001-create-genres
CREATE TABLE genres (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL UNIQUE
);

--changeset ranobereader:002-create-novels
CREATE TABLE novels (
    id          BIGSERIAL PRIMARY KEY,
    title_ru    VARCHAR(255) NOT NULL,
    title_en    VARCHAR(255),
    slug        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    cover_image VARCHAR(512),
    author      VARCHAR(255) DEFAULT 'Неизвестен',
    status      VARCHAR(50)  NOT NULL DEFAULT 'ONGOING', -- COMPLETED, PAUSED, ONGOING
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--changeset ranobereader:003-create-novel-genres
CREATE TABLE novel_genres (
    novel_id BIGINT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE RESTRICT,
    PRIMARY KEY (novel_id, genre_id)
);

--changeset ranobereader:004-create-chapters
CREATE TABLE chapters (
    id             BIGSERIAL PRIMARY KEY,
    novel_id       BIGINT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    tome_number    INT NOT NULL DEFAULT 1,
    chapter_number NUMERIC(6, 2) NOT NULL,
    title          VARCHAR(255),
    content        TEXT NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_novel_chapter UNIQUE (novel_id, tome_number, chapter_number)
);

--changeset ranobereader:005-create-bookmarks
CREATE TABLE bookmarks (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    novel_id    BIGINT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    status      VARCHAR(50) NOT NULL, -- 'READING', 'PLANNED', 'COMPLETED', 'DROPPED'
    PRIMARY KEY (user_id, novel_id)
);

--changeset ranobereader:006-create-ratings
CREATE TABLE ratings (
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    novel_id    BIGINT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    rating      INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    PRIMARY KEY (user_id, novel_id)
);
