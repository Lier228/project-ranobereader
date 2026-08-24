package kz.mad.mangareader.mapper;

import kz.mad.mangareader.dto.novel.ChapterRequest;
import kz.mad.mangareader.dto.novel.ChapterResponse;
import kz.mad.mangareader.entity.Chapter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    Chapter toEntity(ChapterRequest request);

    ChapterResponse toResponse(Chapter chapter);
}