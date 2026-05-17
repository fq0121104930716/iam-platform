package iam.platform.admin.application.assembler;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.admin.domain.model.entity.AuditLog;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuditLogAssembler {

    @Mapping(target = "eventType",
            expression = "java(domain.getEventType() != null ? domain.getEventType().name() : null)")
    @Mapping(target = "eventCategory",
            expression = "java(domain.getEventCategory() != null ? domain.getEventCategory().name() : null)")
    @Mapping(target = "result",
            expression = "java(domain.getResult() != null ? domain.getResult().name() : null)")
    AuditLogResponse toResponse(AuditLog domain);

    List<AuditLogResponse> toResponseList(List<AuditLog> domains);
}
