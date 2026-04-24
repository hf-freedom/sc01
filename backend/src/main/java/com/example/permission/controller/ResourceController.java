package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Api;
import com.example.permission.entity.Resource;
import com.example.permission.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/list")
    public Result<List<Resource>> list(@RequestParam(required = false) Integer type) {
        List<Resource> resources = resourceService.list(type);
        return Result.success(resources);
    }

    @GetMapping("/tree")
    public Result<List<Resource>> tree() {
        List<Resource> tree = resourceService.getTree();
        return Result.success(tree);
    }

    @GetMapping("/{id}")
    public Result<Resource> getById(@PathVariable String id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }

    @PostMapping
    public Result<Resource> create(@RequestBody Resource resource) {
        try {
            Resource created = resourceService.create(resource);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Resource> update(@RequestBody Resource resource) {
        try {
            Resource updated = resourceService.update(resource);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            resourceService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/toggle/{id}")
    public Result<Void> toggleStatus(@PathVariable String id) {
        try {
            resourceService.toggleStatus(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{resourceId}/api")
    public Result<Api> addApi(@PathVariable String resourceId, @RequestBody Api api) {
        try {
            Api created = resourceService.addApi(resourceId, api);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{resourceId}/api/{apiId}")
    public Result<Void> deleteApi(@PathVariable String resourceId, @PathVariable String apiId) {
        try {
            resourceService.deleteApi(resourceId, apiId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
