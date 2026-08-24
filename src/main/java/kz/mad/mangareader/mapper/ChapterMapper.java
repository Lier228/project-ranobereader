package kz.mad.mangareader.mapper;

import kz.mad.mangareader.dto.novel.ChapterRequest;
import kz.mad.mangareader.dto.novel.ChapterResponse;
import kz.mad.mangareader.entity.Chapter;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface ChapterMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Chapter toEntity(ChapterRequest request);

    @Mapping(target = "novelId", source = "novel.id")
    ChapterResponse toResponse(Chapter chapter);
}