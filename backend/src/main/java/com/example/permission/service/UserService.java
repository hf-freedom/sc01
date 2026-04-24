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
public class UserService {

    private final OrganizationService organizationService;
    private final RoleService roleService;
    private final ResourceService resourceService;

    public UserService(OrganizationService organizationService, RoleService roleService, ResourceService resourceService) {
        this.organizationService = organizationService;
        this.roleService = roleService;
        this.resourceService = resourceService;
    }

    public User login(String username, String password) {
        Map<String, User> users = DataStore.getUsers();
        for (User user : users.values()) {
            if (username.equals(user.getUsername()) && password.equals(user.getPassword())
                    && user.getStatus() == 1 && user.getIsDeleted() == 0) {
                return enrichUserInfo(user);
            }
        }
        return null;
    }

    public User enrichUserInfo(User user) {
        if (user == null) {
            return null;
        }
        User result = new User();
        result.setId(user.getId());
        result.setUsername(user.getUsername());
        result.setRealName(user.getRealName());
        result.setEmail(user.getEmail());
        result.setPhone(user.getPhone());
        result.setOrganizationId(user.getOrganizationId());
        result.setStatus(user.getStatus());
        result.setCreateTime(user.getCreateTime());
        result.setUpdateTime(user.getUpdateTime());
        result.setRoleIds(new ArrayList<>(user.getRoleIds()));

        if (StrUtil.isNotBlank(user.getOrganizationId())) {
            Organization org = organizationService.getById(user.getOrganizationId());
            result.setOrganization(org);
        }

        List<Role> roles = new ArrayList<>();
        Set<String> allResourceIds = new HashSet<>();
        for (String roleId : user.getRoleIds()) {
            Role role = roleService.getById(roleId);
            if (role != null && role.getStatus() == 1) {
                roles.add(role);
                allResourceIds.addAll(role.getResourceIds());
            }
        }
        result.setRoles(roles);

        Set<String> completeResourceIds = new HashSet<>(allResourceIds);
        for (String resourceId : allResourceIds) {
            collectParentResourceIds(resourceId, completeResourceIds);
        }

        List<Resource> allResources = new ArrayList<>();
        Set<String> permissions = new HashSet<>();
        for (String resourceId : completeResourceIds) {
            Resource resource = resourceService.getById(resourceId);
            if (resource != null && resource.getStatus() == 1) {
                allResources.add(resource);
                if (StrUtil.isNotBlank(resource.getPermission())) {
                    permissions.add(resource.getPermission());
                }
            }
        }

        List<Resource> menuTree = resourceService.buildTree(allResources);
        result.setMenus(menuTree);
        result.setPermissions(new ArrayList<>(permissions));

        return result;
    }

    public List<User> list(String keyword, String organizationId) {
        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);

        return DataStore.getUsers().values().stream()
                .filter(user -> user.getIsDeleted() == 0)
                .filter(user -> {
                    if (StrUtil.isNotBlank(organizationId)) {
                        return organizationId.equals(user.getOrganizationId());
                    }
                    return accessibleOrgIds.contains(user.getOrganizationId());
                })
                .filter(user -> {
                    if (StrUtil.isBlank(keyword)) {
                        return true;
                    }
                    String lowerKeyword = keyword.toLowerCase();
                    return user.getUsername().toLowerCase().contains(lowerKeyword)
                            || user.getRealName().toLowerCase().contains(lowerKeyword)
                            || user.getPhone().contains(lowerKeyword);
                })
                .sorted(Comparator.comparing(User::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    public User getById(String id) {
        User user = DataStore.getUsers().get(id);
        if (user == null || user.getIsDeleted() == 1) {
            return null;
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(user.getOrganizationId())) {
            return null;
        }
        return user;
    }

    public User create(User user) {
        String id = IdUtil.simpleUUID();
        user.setId(id);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getRoleIds() == null) {
            user.setRoleIds(new ArrayList<>());
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(user.getOrganizationId())) {
            throw new RuntimeException("无权限在该组织机构下创建用户");
        }

        DataStore.getUsers().put(id, user);
        return user;
    }

    public User update(User user) {
        User existingUser = getById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (user.getOrganizationId() != null && !accessibleOrgIds.contains(user.getOrganizationId())) {
            throw new RuntimeException("无权限将用户移动到该组织机构");
        }

        if (StrUtil.isNotBlank(user.getUsername())) {
            existingUser.setUsername(user.getUsername());
        }
        if (StrUtil.isNotBlank(user.getPassword())) {
            existingUser.setPassword(user.getPassword());
        }
        if (StrUtil.isNotBlank(user.getRealName())) {
            existingUser.setRealName(user.getRealName());
        }
        if (StrUtil.isNotBlank(user.getEmail())) {
            existingUser.setEmail(user.getEmail());
        }
        if (StrUtil.isNotBlank(user.getPhone())) {
            existingUser.setPhone(user.getPhone());
        }
        if (StrUtil.isNotBlank(user.getOrganizationId())) {
            existingUser.setOrganizationId(user.getOrganizationId());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getRoleIds() != null) {
            existingUser.setRoleIds(user.getRoleIds());
        }
        existingUser.setUpdateTime(LocalDateTime.now());

        DataStore.getUsers().put(existingUser.getId(), existingUser);
        return existingUser;
    }

    public void delete(String id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsDeleted(1);
        user.setUpdateTime(LocalDateTime.now());
        DataStore.getUsers().put(id, user);
    }

    public void toggleStatus(String id) {
        User user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        user.setUpdateTime(LocalDateTime.now());
        DataStore.getUsers().put(id, user);
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

    private void collectParentResourceIds(String resourceId, Set<String> resourceIds) {
        if (resourceId == null || "0".equals(resourceId)) {
            return;
        }
        Resource resource = resourceService.getById(resourceId);
        if (resource == null) {
            return;
        }
        String parentId = resource.getParentId();
        if (parentId != null && !"0".equals(parentId) && !resourceIds.contains(parentId)) {
            resourceIds.add(parentId);
            collectParentResourceIds(parentId, resourceIds);
        }
    }
}
