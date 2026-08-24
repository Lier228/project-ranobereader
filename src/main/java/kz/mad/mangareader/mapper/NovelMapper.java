package kz.mad.mangareader.mapper;

import kz.mad.mangareader.dto.novel.NovelRequest;
import kz.mad.mangareader.dto.novel.NovelResponse;
import kz.mad.mangareader.entity.Novel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface NovelMapper {
    Novel toEntity(NovelRequest request);

    void updateEntity(NovelRequest request, @MappingTarget Novel novel);

    NovelResponse toResponse(Novel novel);
}