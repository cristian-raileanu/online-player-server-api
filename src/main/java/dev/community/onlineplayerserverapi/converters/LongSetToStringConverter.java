package dev.community.onlineplayerserverapi.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class LongSetToStringConverter implements AttributeConverter<Set<Long>, String> {

    private static final String DELIMITER = ",";

    @Override
    public String convertToDatabaseColumn(Set<Long> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }
        return attribute.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Set<Long> convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return Collections.emptySet();
        }
        return Arrays.stream(dbData.split(DELIMITER))
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}
