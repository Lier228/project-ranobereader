package kz.mad.mangareader.mapper;

import kz.mad.mangareader.dto.novel.ChapterRequest;
import kz.mad.mangareader.dto.novel.ChapterResponse;
import kz.mad.mangareader.entity.Chapter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChapterMapper {
    @Mapping(target = "novel", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Chapter toEntity(ChapterRequest request);

    @Mapping(target = "novelId", source = "novel.id")
    ChapterResponse toResponse(Chapter chapter);
}