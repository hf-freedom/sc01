package com.example.permission.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.permission.entity.Api;
import com.example.permission.entity.Resource;
import com.example.permission.store.DataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    public List<Resource> list(Integer type) {
        return DataStore.getResources().values().stream()
                .filter(resource -> resource.getIsDeleted() == 0)
                .filter(resource -> type == null || type.equals(resource.getType()))
                .sorted(Comparator.comparing(Resource::getSort))
                .collect(Collectors.toList());
    }

    public Resource getById(String id) {
        Resource resource = DataStore.getResources().get(id);
        if (resource == null || resource.getIsDeleted() == 1) {
            return null;
        }
        return resource;
    }

    public List<Resource> buildTree(List<Resource> allResources) {
        Map<String, List<Resource>> parentMap = allResources.stream()
                .collect(Collectors.groupingBy(Resource::getParentId));

        for (Resource resource : allResources) {
            resource.setChildren(parentMap.getOrDefault(resource.getId(), new ArrayList<>()));
        }

        return allResources.stream()
                .filter(resource -> "0".equals(resource.getParentId()))
                .collect(Collectors.toList());
    }

    public List<Resource> getTree() {
        List<Resource> allResources = DataStore.getResources().values().stream()
                .filter(resource -> resource.getIsDeleted() == 0)
                .filter(resource -> resource.getStatus() == 1)
                .sorted(Comparator.comparing(Resource::getSort))
                .collect(Collectors.toList());
        return buildTree(allResources);
    }

    public Resource create(Resource resource) {
        String id = IdUtil.simpleUUID();
        resource.setId(id);
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());
        resource.setIsDeleted(0);
        if (resource.getStatus() == null) {
            resource.setStatus(1);
        }
        if (resource.getSort() == null) {
            resource.setSort(1);
        }
        if (resource.getApis() == null) {
            resource.setApis(new ArrayList<>());
        }

        DataStore.getResources().put(id, resource);
        return resource;
    }

    public Resource update(Resource resource) {
        Resource existingResource = getById(resource.getId());
        if (existingResource == null) {
            throw new RuntimeException("资源不存在");
        }

        if (StrUtil.isNotBlank(resource.getName())) {
            existingResource.setName(resource.getName());
        }
        if (StrUtil.isNotBlank(resource.getCode())) {
            existingResource.setCode(resource.getCode());
        }
        if (resource.getType() != null) {
            existingResource.setType(resource.getType());
        }
        if (StrUtil.isNotBlank(resource.getPath())) {
            existingResource.setPath(resource.getPath());
        }
        if (StrUtil.isNotBlank(resource.getComponent())) {
            existingResource.setComponent(resource.getComponent());
        }
        if (StrUtil.isNotBlank(resource.getIcon())) {
            existingResource.setIcon(resource.getIcon());
        }
        if (resource.getSort() != null) {
            existingResource.setSort(resource.getSort());
        }
        if (StrUtil.isNotBlank(resource.getPermission())) {
            existingResource.setPermission(resource.getPermission());
        }
        if (StrUtil.isNotBlank(resource.getDescription())) {
            existingResource.setDescription(resource.getDescription());
        }
        if (resource.getStatus() != null) {
            existingResource.setStatus(resource.getStatus());
        }
        if (resource.getApis() != null) {
            existingResource.setApis(resource.getApis());
        }
        existingResource.setUpdateTime(LocalDateTime.now());

        DataStore.getResources().put(existingResource.getId(), existingResource);
        return existingResource;
    }

    public void delete(String id) {
        Resource resource = getById(id);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        long childCount = DataStore.getResources().values().stream()
                .filter(r -> r.getIsDeleted() == 0 && id.equals(r.getParentId()))
                .count();
        if (childCount > 0) {
            throw new RuntimeException("该资源下存在子资源，无法删除");
        }

        resource.setIsDeleted(1);
        resource.setUpdateTime(LocalDateTime.now());
        DataStore.getResources().put(id, resource);
    }

    public void toggleStatus(String id) {
        Resource resource = getById(id);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }
        resource.setStatus(resource.getStatus() == 1 ? 0 : 1);
        resource.setUpdateTime(LocalDateTime.now());
        DataStore.getResources().put(id, resource);
    }

    public Api addApi(String resourceId, Api api) {
        Resource resource = getById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        String apiId = IdUtil.simpleUUID();
        api.setId(apiId);
        api.setResourceId(resourceId);
        api.setCreateTime(LocalDateTime.now());
        api.setUpdateTime(LocalDateTime.now());
        api.setIsDeleted(0);
        if (api.getStatus() == null) {
            api.setStatus(1);
        }

        DataStore.getApis().put(apiId, api);
        resource.getApis().add(api);
        resource.setUpdateTime(LocalDateTime.now());
        DataStore.getResources().put(resourceId, resource);

        return api;
    }

    public void deleteApi(String resourceId, String apiId) {
        Resource resource = getById(resourceId);
        if (resource == null) {
            throw new RuntimeException("资源不存在");
        }

        Api api = DataStore.getApis().get(apiId);
        if (api != null) {
            api.setIsDeleted(1);
            api.setUpdateTime(LocalDateTime.now());
            DataStore.getApis().put(apiId, api);
        }

        resource.getApis().removeIf(a -> apiId.equals(a.getId()));
        resource.setUpdateTime(LocalDateTime.now());
        DataStore.getResources().put(resourceId, resource);
    }

    public Map<String, Api> getApiMap() {
        return DataStore.getApis();
    }
}
