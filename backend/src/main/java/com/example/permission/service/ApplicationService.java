package com.example.permission.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.permission.context.UserContext;
import com.example.permission.entity.Application;
import com.example.permission.entity.Organization;
import com.example.permission.entity.User;
import com.example.permission.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final OrganizationService organizationService;

    public ApplicationService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public List<Application> list(String keyword, Integer type, String organizationId) {
        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);

        return DataStore.getApplications().values().stream()
                .filter(app -> app.getIsDeleted() == 0)
                .filter(app -> {
                    if (StrUtil.isNotBlank(organizationId)) {
                        return organizationId.equals(app.getOrganizationId());
                    }
                    return accessibleOrgIds.contains(app.getOrganizationId());
                })
                .filter(app -> {
                    if (StrUtil.isBlank(keyword)) {
                        return true;
                    }
                    String lowerKeyword = keyword.toLowerCase();
                    return app.getName().toLowerCase().contains(lowerKeyword)
                            || app.getDescription().toLowerCase().contains(lowerKeyword);
                })
                .filter(app -> type == null || type.equals(app.getType()))
                .sorted(Comparator.comparing(Application::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    public Application getById(String id) {
        Application app = DataStore.getApplications().get(id);
        if (app == null || app.getIsDeleted() == 1) {
            return null;
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(app.getOrganizationId())) {
            return null;
        }

        if (StrUtil.isNotBlank(app.getOrganizationId())) {
            Organization org = organizationService.getById(app.getOrganizationId());
            app.setOrganization(org);
        }
        return app;
    }

    public Application create(Application application) {
        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(application.getOrganizationId())) {
            throw new RuntimeException("无权限在该组织机构下创建应用");
        }

        String id = IdUtil.simpleUUID();
        application.setId(id);
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        application.setIsDeleted(0);
        if (application.getStatus() == null) {
            application.setStatus(1);
        }

        DataStore.getApplications().put(id, application);
        return application;
    }

    public Application update(Application application) {
        Application existingApp = getById(application.getId());
        if (existingApp == null) {
            throw new RuntimeException("应用不存在");
        }

        if (StrUtil.isNotBlank(application.getName())) {
            existingApp.setName(application.getName());
        }
        if (application.getType() != null) {
            existingApp.setType(application.getType());
        }
        if (StrUtil.isNotBlank(application.getDescription())) {
            existingApp.setDescription(application.getDescription());
        }
        if (application.getStatus() != null) {
            existingApp.setStatus(application.getStatus());
        }
        existingApp.setUpdateTime(LocalDateTime.now());

        DataStore.getApplications().put(existingApp.getId(), existingApp);
        return existingApp;
    }

    public void delete(String id) {
        Application app = getById(id);
        if (app == null) {
            throw new RuntimeException("应用不存在");
        }
        app.setIsDeleted(1);
        app.setUpdateTime(LocalDateTime.now());
        DataStore.getApplications().put(id, app);
    }

    public void toggleStatus(String id) {
        Application app = getById(id);
        if (app == null) {
            throw new RuntimeException("应用不存在");
        }
        app.setStatus(app.getStatus() == 1 ? 0 : 1);
        app.setUpdateTime(LocalDateTime.now());
        DataStore.getApplications().put(id, app);
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
