package com.rocky.cocoa.entity.plugin;

import com.rocky.cocoa.core.util.JsonUtil;
import lombok.SneakyThrows;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

@Converter
public class ParamJpaConverterJson implements AttributeConverter<List, String> {

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List stringObjectMap) {
        return JsonUtil.toJson(stringObjectMap);
    }

    @SneakyThrows
    @Override
    public List<PackageParam> convertToEntityAttribute(String s) {
        return JsonUtil.fromJsonList(PackageParam.class,s);
    }
}
