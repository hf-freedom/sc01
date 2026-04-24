package com.example.permission.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.permission.context.UserContext;
import com.example.permission.entity.Organization;
import com.example.permission.entity.User;
import com.example.permission.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    public List<Organization> list() {
        List<Organization> allOrgs = DataStore.getOrganizations().values().stream()
                .filter(org -> org.getIsDeleted() == 0)
                .sorted(Comparator.comparing(Organization::getSort))
                .collect(Collectors.toList());
        return buildTree(allOrgs);
    }

    public Organization getById(String id) {
        Organization org = DataStore.getOrganizations().get(id);
        if (org == null || org.getIsDeleted() == 1) {
            return null;
        }
        return org;
    }

    public List<Organization> getByParentId(String parentId) {
        return DataStore.getOrganizations().values().stream()
                .filter(org -> org.getIsDeleted() == 0)
                .filter(org -> parentId.equals(org.getParentId()))
                .sorted(Comparator.comparing(Organization::getSort))
                .collect(Collectors.toList());
    }

    public List<Organization> buildTree(List<Organization> allOrgs) {
        Map<String, List<Organization>> parentMap = allOrgs.stream()
                .collect(Collectors.groupingBy(Organization::getParentId));

        for (Organization org : allOrgs) {
            org.setChildren(parentMap.getOrDefault(org.getId(), new ArrayList<>()));
        }

        return allOrgs.stream()
                .filter(org -> "0".equals(org.getParentId()))
                .collect(Collectors.toList());
    }

    public Organization create(Organization organization) {
        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);

        String parentId = organization.getParentId();
        if (!"0".equals(parentId) && !accessibleOrgIds.contains(parentId)) {
            throw new RuntimeException("无权限在该组织机构下创建子机构");
        }

        String id = IdUtil.simpleUUID();
        organization.setId(id);
        organization.setCreateTime(LocalDateTime.now());
        organization.setUpdateTime(LocalDateTime.now());
        organization.setIsDeleted(0);
        if (organization.getStatus() == null) {
            organization.setStatus(1);
        }
        if (organization.getSort() == null) {
            organization.setSort(1);
        }

        Organization parentOrg = getById(organization.getParentId());
        if (parentOrg != null) {
            organization.setLevel(parentOrg.getLevel() + 1);
        } else {
            organization.setLevel(1);
        }

        DataStore.getOrganizations().put(id, organization);
        return organization;
    }

    public Organization update(Organization organization) {
        Organization existingOrg = getById(organization.getId());
        if (existingOrg == null) {
            throw new RuntimeException("组织机构不存在");
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(existingOrg.getId())) {
            throw new RuntimeException("无权限修改该组织机构");
        }

        if (StrUtil.isNotBlank(organization.getName())) {
            existingOrg.setName(organization.getName());
        }
        if (StrUtil.isNotBlank(organization.getCode())) {
            existingOrg.setCode(organization.getCode());
        }
        if (StrUtil.isNotBlank(organization.getDescription())) {
            existingOrg.setDescription(organization.getDescription());
        }
        if (organization.getSort() != null) {
            existingOrg.setSort(organization.getSort());
        }
        if (organization.getStatus() != null) {
            existingOrg.setStatus(organization.getStatus());
        }
        existingOrg.setUpdateTime(LocalDateTime.now());

        DataStore.getOrganizations().put(existingOrg.getId(), existingOrg);
        return existingOrg;
    }

    public void delete(String id) {
        Organization org = getById(id);
        if (org == null) {
            throw new RuntimeException("组织机构不存在");
        }

        User currentUser = UserContext.getCurrentUser();
        Set<String> accessibleOrgIds = getAccessibleOrganizationIds(currentUser);
        if (!accessibleOrgIds.contains(org.getId())) {
            throw new RuntimeException("无权限删除该组织机构");
        }

        List<Organization> children = getByParentId(id);
        if (!children.isEmpty()) {
            throw new RuntimeException("该组织机构下存在子机构，无法删除");
        }

        long userCount = DataStore.getUsers().values().stream()
                .filter(u -> u.getIsDeleted() == 0 && id.equals(u.getOrganizationId()))
                .count();
        if (userCount > 0) {
            throw new RuntimeException("该组织机构下存在用户，无法删除");
        }

        org.setIsDeleted(1);
        org.setUpdateTime(LocalDateTime.now());
        DataStore.getOrganizations().put(id, org);
    }

    private Set<String> getAccessibleOrganizationIds(User currentUser) {
        Set<String> orgIds = new HashSet<>();
        if (currentUser == null || currentUser.getOrganizationId() == null) {
            return orgIds;
        }

        Organization currentOrg = getById(currentUser.getOrganizationId());
        if (currentOrg == null) {
            return orgIds;
        }

        orgIds.add(currentOrg.getId());
        collectChildOrgIds(currentOrg.getId(), orgIds);
        return orgIds;
    }

    private void collectChildOrgIds(String parentId, Set<String> orgIds) {
        List<Organization> children = getByParentId(parentId);
        for (Organization child : children) {
            orgIds.add(child.getId());
            collectChildOrgIds(child.getId(), orgIds);
        }
    }
}
