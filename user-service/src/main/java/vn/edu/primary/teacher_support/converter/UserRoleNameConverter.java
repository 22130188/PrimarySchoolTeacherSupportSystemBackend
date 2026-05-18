package vn.edu.primary.teacher_support.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.edu.primary.teacher_support.entity.Role;

@Converter(autoApply = false)
public class UserRoleNameConverter implements AttributeConverter<Role.RoleName, String> {

    @Override
    public String convertToDatabaseColumn(Role.RoleName attribute) {
        if (attribute == null) {
            return null;
        }

        return switch (attribute) {
            case STUDENT -> "buyer";
            case TEACHER -> "seller";
            case ADMIN -> "admin";
        };
    }

    @Override
    public Role.RoleName convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        return switch (dbData.toLowerCase()) {
            case "buyer" -> Role.RoleName.STUDENT;
            case "seller" -> Role.RoleName.TEACHER;
            case "admin" -> Role.RoleName.ADMIN;
            default -> throw new IllegalArgumentException("Unknown role value from database: " + dbData);
        };
    }
}
