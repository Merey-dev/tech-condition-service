package kz.kus.sa.tech.condition.dao.mapper;

import kz.kus.sa.dictionary.api.DictionaryApiService;
import kz.kus.sa.dictionary.dto.DictionaryValueDto;
import kz.kus.sa.tech.condition.dao.entity.HistoryEntity;
import kz.kus.sa.tech.condition.dto.history.HistoryDto;
import kz.kus.sa.tech.condition.dto.history.HistorySignDto;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Mapper(componentModel = "spring", uses = {
        ExternalUserMapper.class
})
public abstract class HistoryMapper {

    @Autowired
    private DictionaryApiService dictionaryApiService;

    @Mapping(target = "eventRu", expression = "java(getEvent(entity).get(\"ru\"))")
    @Mapping(target = "eventKk", expression = "java(getEvent(entity).get(\"kk\"))")
    @Mapping(target = "statusRu", expression = "java(getStatus(entity).get(\"ru\"))")
    @Mapping(target = "statusKk", expression = "java(getStatus(entity).get(\"kk\"))")
    @Mapping(target = "executionStatusRu", expression = "java(getStatus(entity).get(\"ru\"))")
    @Mapping(target = "executionStatusKk", expression = "java(getStatus(entity).get(\"kk\"))")
    @Mapping(target = "sign", source = "entity", qualifiedByName = "sign")
    public abstract HistoryDto toDto(HistoryEntity entity);

    public abstract List<HistoryDto> toDtoList(List<HistoryEntity> entityList);

    protected Map<String, String> getEvent(HistoryEntity entity) {
        Map<String, String> event = new HashMap<>();
        try {
            String code = "events__" + entity.getEvent().name();
            DictionaryValueDto dictionaryValueDto = dictionaryApiService.findDictionaryValueByCode(code);
            event.put("ru", dictionaryValueDto.getNameRu());
            event.put("kk", dictionaryValueDto.getNameKz());
        } catch (Exception e) {
            log.error("Dictionary value event not found: {}", e.getMessage());
            event.put("ru", null);
            event.put("kk", null);
        }
        return event;
    }

    protected Map<String, String> getStatus(HistoryEntity entity) {
        Map<String, String> status = new HashMap<>();
        try {
            DictionaryValueDto dictionaryValueDto = dictionaryApiService.findDictionaryValueByCode(entity.getStatus());
            status.put("ru", dictionaryValueDto.getNameRu());
            status.put("kk", dictionaryValueDto.getNameKz());
        } catch (Exception e) {
            log.error("Dictionary value status not found: {}", e.getMessage());
            status.put("ru", null);
            status.put("kk", null);
        }
        return status;
    }

    @Named("sign")
    protected HistorySignDto sign(HistoryEntity entity) {
        //todo sign
        return null;
    }
}
