package iam.platform.admin.application.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.admin.domain.model.entity.AuditLog;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogAssembler {

        @Mapping(target = "eventType", source = "eventType", qualifiedByName = "eventTypeToString")
        @Mapping(target = "eventCategory", source = "eventCategory",
                        qualifiedByName = "eventCategoryToString")
        @Mapping(target = "result", source = "result", qualifiedByName = "resultToString")
        AuditLogResponse toResponse(AuditLog domain);

        List<AuditLogResponse> toResponseList(List<AuditLog> domains);

        @Named("eventTypeToString")
        default String eventTypeToString(AuditEventType eventType) {
                return eventType != null ? eventType.name() : null;
        }

        @Named("eventCategoryToString")
        default String eventCategoryToString(EventCategory eventCategory) {
                return eventCategory != null ? eventCategory.name() : null;
        }

        @Named("resultToString")
        default String resultToString(AuditResult result) {
                return result != null ? result.name() : null;
        }
}
