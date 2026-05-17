package iam.platform.admin.infrastructure.persistence.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import iam.platform.admin.domain.model.entity.ApplicationResource;
import iam.platform.admin.domain.model.entity.ApplicationTenantMapping;
import iam.platform.admin.domain.model.entity.PlatformMenu;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.admin.domain.model.entity.TenantAccount;
import iam.platform.admin.domain.model.entity.TenantAccountOrganizationMapping;
import iam.platform.admin.domain.model.entity.TenantAccountRoleMapping;
import iam.platform.admin.domain.model.entity.TenantMenuConfig;
import iam.platform.admin.domain.model.entity.UserRoleMapping;
import iam.platform.admin.domain.model.entity.UserTenantMapping;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationResourcePO;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationTenantMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.PlatformMenuPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountOrganizationMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountRoleMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantMenuConfigPO;
import iam.platform.admin.infrastructure.persistence.entity.TenantPO;
import iam.platform.admin.infrastructure.persistence.entity.UserRoleMappingPO;
import iam.platform.admin.infrastructure.persistence.entity.UserTenantMappingPO;

import java.util.List;

/**
 * MapStruct mapper for converting between Domain Entities and POs. This provides a unified approach
 * for object mapping across the infrastructure layer.
 */
@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface DomainPoMapper {

        // ==================== Tenant Conversions ====================

        @Mapping(target = "status",
                        expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"ACTIVE\")")
        TenantPO toTenantPO(Tenant domain);

        @Mapping(target = "status",
                        expression = "java(po.getStatus() != null ? iam.platform.common.model.enums.TenantStatus.valueOf(po.getStatus()) : null)")
        Tenant toTenantDomain(TenantPO po);

        List<Tenant> toTenantDomainList(List<TenantPO> pos);

        // ==================== TenantAccount Conversions ====================

        @Mapping(target = "status",
                        expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"ACTIVE\")")
        TenantAccountPO toTenantAccountPO(TenantAccount domain);

        @Mapping(target = "status",
                        expression = "java(po.getStatus() != null ? iam.platform.common.model.enums.AccountStatus.valueOf(po.getStatus()) : null)")
        @Mapping(target = "roles", ignore = true)
        TenantAccount toTenantAccountDomain(TenantAccountPO po);

        @Mapping(target = "roles", ignore = true)
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

        // ==================== UserTenantMapping Conversions ====================

        @Mapping(target = "status",
                        expression = "java(domain.getStatus() != null ? domain.getStatus().name() : \"ACTIVE\")")
        UserTenantMappingPO toUserTenantMappingPO(UserTenantMapping domain);

        @Mapping(target = "status",
                        expression = "java(po.getStatus() != null ? iam.platform.common.model.enums.UserTenantStatus.valueOf(po.getStatus()) : null)")
        UserTenantMapping toUserTenantMappingDomain(UserTenantMappingPO po);

        List<UserTenantMapping> toUserTenantMappingDomainList(List<UserTenantMappingPO> pos);

        // ==================== UserRoleMapping Conversions ====================

        UserRoleMappingPO toUserRoleMappingPO(UserRoleMapping domain);

        UserRoleMapping toUserRoleMappingDomain(UserRoleMappingPO po);

        List<UserRoleMapping> toUserRoleMappingDomainList(List<UserRoleMappingPO> pos);

        // ==================== PlatformMenu Conversions ====================

        PlatformMenuPO toPlatformMenuPO(PlatformMenu domain);

        PlatformMenu toPlatformMenuDomain(PlatformMenuPO po);

        List<PlatformMenu> toPlatformMenuDomainList(List<PlatformMenuPO> pos);

        // ==================== TenantMenuConfig Conversions ====================

        TenantMenuConfigPO toTenantMenuConfigPO(TenantMenuConfig domain);

        TenantMenuConfig toTenantMenuConfigDomain(TenantMenuConfigPO po);

        List<TenantMenuConfig> toTenantMenuConfigDomainList(List<TenantMenuConfigPO> pos);

        // ==================== ApplicationResource Conversions ====================

        @Mapping(target = "resourceType",
                        expression = "java(domain.getResourceType() != null ? domain.getResourceType().name() : null)")
        ApplicationResourcePO toApplicationResourcePO(ApplicationResource domain);

        @Mapping(target = "resourceType",
                        expression = "java(po.getResourceType() != null ? iam.platform.common.model.enums.ResourceType.valueOf(po.getResourceType()) : null)")
        ApplicationResource toApplicationResourceDomain(ApplicationResourcePO po);

        List<ApplicationResource> toApplicationResourceDomainList(List<ApplicationResourcePO> pos);

        // ==================== ApplicationTenantMapping Conversions ====================

        ApplicationTenantMappingPO toApplicationTenantMappingPO(ApplicationTenantMapping domain);

        ApplicationTenantMapping toApplicationTenantMappingDomain(ApplicationTenantMappingPO po);

        List<ApplicationTenantMapping> toApplicationTenantMappingDomainList(
                        List<ApplicationTenantMappingPO> pos);
}
