package com.example.permission.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.permission.context.UserContext;
import com.example.permission.entity.*;
import com.example.permission.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private final OrganizationService organizationService;
    private final ResourceService resourceService;

    public RoleService(OrganizationService organizationService, ResourceService resourceService) {
        this.organizationService = organizationService;
        this.resourceService = resourceService;
    }

    public List<Role> list(String keyword, String organizationId) {
        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);

        return DataStore.getRoles().values().stream()
                .filter(role -> role.getIsDeleted() == 0)
                .filter(role -> {
                    if (StrUtil.isNotBlank(organizationId)) {
                        return organizationId.equals(role.getOrganizationId());
                    }
                    return accessibleOrgIds.contains(role.getOrganizationId());
                })
                .filter(role -> {
                    if (StrUtil.isBlank(keyword)) {
                        return true;
                    }
                    String lowerKeyword = keyword.toLowerCase();
                    return role.getName().toLowerCase().contains(lowerKeyword)
                            || role.getCode().toLowerCase().contains(lowerKeyword);
                })
                .sorted(Comparator.comparing(Role::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    public Role getById(String id) {
        Role role = DataStore.getRoles().get(id);
        if (role == null || role.getIsDeleted() == 1) {
            return null;
        }
        return role;
    }

    public Role create(Role role) {
        String id = IdUtil.simpleUUID();
        role.setId(id);
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        role.setIsDeleted(0);
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        if (role.getResourceIds() == null) {
            role.setResourceIds(new ArrayList<>());
        }
        if (role.getUserIds() == null) {
            role.setUserIds(new ArrayList<>());
        }
        if (role.getMenuIds() == null) {
            role.setMenuIds(new ArrayList<>());
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(role.getOrganizationId())) {
            throw new RuntimeException("无权限在该组织机构下创建角色");
        }

        DataStore.getRoles().put(id, role);
        return role;
    }

    public Role update(Role role) {
        Role existingRole = getById(role.getId());
        if (existingRole == null) {
            throw new RuntimeException("角色不存在");
        }

        if (StrUtil.isNotBlank(role.getName())) {
            existingRole.setName(role.getName());
        }
        if (StrUtil.isNotBlank(role.getCode())) {
            existingRole.setCode(role.getCode());
        }
        if (StrUtil.isNotBlank(role.getDescription())) {
            existingRole.setDescription(role.getDescription());
        }
        if (role.getStatus() != null) {
            existingRole.setStatus(role.getStatus());
        }
        existingRole.setUpdateTime(LocalDateTime.now());

        DataStore.getRoles().put(existingRole.getId(), existingRole);
        return existingRole;
    }

    public void delete(String id) {
        Role role = getById(id);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        if (!role.getUserIds().isEmpty()) {
            throw new RuntimeException("该角色下存在用户，无法删除");
        }

        role.setIsDeleted(1);
        role.setUpdateTime(LocalDateTime.now());
        DataStore.getRoles().put(id, role);
    }

    public void bindResources(String roleId, List<String> resourceIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        role.setResourceIds(resourceIds != null ? resourceIds : new ArrayList<>());
        role.setUpdateTime(LocalDateTime.now());
        DataStore.getRoles().put(roleId, role);
    }

    public void bindUsers(String roleId, List<String> userIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }

        List<String> oldUserIds = role.getUserIds();
        for (String oldUserId : oldUserIds) {
            User user = DataStore.getUsers().get(oldUserId);
            if (user != null) {
                user.getRoleIds().remove(roleId);
            }
        }

        if (userIds != null) {
            for (String userId : userIds) {
                User user = DataStore.getUsers().get(userId);
                if (user != null && !user.getRoleIds().contains(roleId)) {
                    user.getRoleIds().add(roleId);
                }
            }
        }

        role.setUserIds(userIds != null ? userIds : new ArrayList<>());
        role.setUpdateTime(LocalDateTime.now());
        DataStore.getRoles().put(roleId, role);
    }

    public void bindMenus(String roleId, List<String> menuIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在");
        }
        role.setMenuIds(menuIds != null ? menuIds : new ArrayList<>());
        role.setUpdateTime(LocalDateTime.now());
        DataStore.getRoles().put(roleId, role);
    }

    public List<Resource> getRoleResources(String roleId) {
        Role role = getById(roleId);
        if (role == null) {
            return new ArrayList<>();
        }

        List<Resource> resources = new ArrayList<>();
        for (String resourceId : role.getResourceIds()) {
            Resource resource = resourceService.getById(resourceId);
            if (resource != null && resource.getStatus() == 1) {
                resources.add(resource);
            }
        }
        return resources;
    }

    private Set<String> getAccessibleOrganizationIds(User currentUser) {
        Set<String> orgIds = new HashSet<>();
        if (currentUser == null || currentUser.getOrganizationId() == null) {
            return orgIds;
        }

        Organization currentOrg = organizationService.getById(currentUser.getOrganizationId());
        if (currentOrg == null) {
            return orgIds;
        }

        orgIds.add(currentOrg.getId());
        collectChildOrgIds(currentOrg.getId(), orgIds);
        return orgIds;
    }

    private void collectChildOrgIds(String parentId, Set<String> orgIds) {
        List<Organization> children = organizationService.getByParentId(parentId);
        for (Organization child : children) {
            orgIds.add(child.getId());
            collectChildOrgIds(child.getId(), orgIds);
        }
    }
}
