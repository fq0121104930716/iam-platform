package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.OrgType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private Long tenantId;
    private String orgCode;
    private String orgName;
    private OrgType orgType;
    private Long parentId;
    private String parentName;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private Long managerId;
    private String managerName;
    private String phone;
    private String email;
    private String status;
    private String description;
    private Integer memberCount;
    private List<OrganizationResponse> children;
    private Boolean isPrimary; // 是否为主组织（用于用户组织关联响应）
    private String position; // 岗位名称（用于用户组织关联响应）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
