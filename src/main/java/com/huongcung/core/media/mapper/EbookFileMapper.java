package com.huongcung.core.media.mapper;

import com.huongcung.core.common.mapper.DomainMapper;
import com.huongcung.core.media.helper.FileUrlHelper;
import com.huongcung.core.media.model.domain.EbookFile;
import com.huongcung.core.media.model.entity.EbookFileEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(
        componentModel = "spring",
        uses = { FileUrlHelper.class }
)
public interface EbookFileMapper extends DomainMapper<EbookFileEntity, EbookFile> {

    @Override
    @Mappings({
            @Mapping(target = "url", source = "url", qualifiedByName = "buildFullUrl")
    })
    EbookFile toDomain(EbookFileEntity entity);
}
