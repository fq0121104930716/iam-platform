package iam.platform.admin.infrastructure.persistence.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.admin.domain.model.entity.TenantAccount;
import iam.platform.admin.domain.model.entity.TenantAccountOrganizationMapping;
import iam.platform.admin.domain.model.entity.TenantAccountRoleMapping;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountOrganizationMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountRoleMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantPO;

import java.util.List;

/**
 * MapStruct mapper for converting between Domain Entities and POs. This
 * provides a unified approach
 * for object mapping across the infrastructure layer.
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface DomainPoMapper {

        // ==================== Tenant Conversions ====================

        @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"ACTIVE\")")
        TenantPO toTenantPO(Tenant domain);

        @Mapping(target = "status", expression = "java(po.getStatus() != null ? iam.platform.common.model.enums.TenantStatus.valueOf(po.getStatus()) : null)")
        Tenant toTenantDomain(TenantPO po);

        List<Tenant> toTenantDomainList(List<TenantPO> pos);

        // ==================== TenantAccount Conversions ====================

        @Mapping(target = "status", expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"ACTIVE\")")
        TenantAccountPO toTenantAccountPO(TenantAccount domain);

        @Mapping(target = "status", expression = "java(po.getStatus() != null ? iam.platform.common.model.enums.AccountStatus.valueOf(po.getStatus()) : null)")
        @Mapping(target = "roles", ignore = true)
        TenantAccount toTenantAccountDomain(TenantAccountPO po);

        List<TenantAccount> toTenantAccountDomainList(List<TenantAccountPO> pos);

        // ==================== TenantAccountOrganizationMapping Conversions
        // ====================

        TenantAccountOrganizationMappingPO toTenantAccountOrganizationMappingPO(
                        TenantAccountOrganizationMapping domain);

        TenantAccountOrganizationMapping toTenantAccountOrganizationMappingDomain(
                        TenantAccountOrganizationMappingPO po);

        List<TenantAccountOrganizationMapping> toTenantAccountOrganizationMappingDomainList(
                        List<TenantAccountOrganizationMappingPO> pos);

        // ==================== TenantAccountRoleMapping Conversions
        // ====================

        TenantAccountRoleMappingPO toTenantAccountRoleMappingPO(TenantAccountRoleMapping domain);

        TenantAccountRoleMapping toTenantAccountRoleMappingDomain(TenantAccountRoleMappingPO po);

        List<TenantAccountRoleMapping> toTenantAccountRoleMappingDomainList(
                        List<TenantAccountRoleMappingPO> pos);
}
