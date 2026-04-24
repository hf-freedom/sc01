package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Resource;
import com.example.permission.entity.Role;
import com.example.permission.service.RoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/list")
    public Result<List<Role>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String organizationId) {
        List<Role> roles = roleService.list(keyword, organizationId);
        return Result.success(roles);
    }

    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable String id) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping
    public Result<Role> create(@RequestBody Role role) {
        try {
            Role created = roleService.create(role);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Role> update(@RequestBody Role role) {
        try {
            Role updated = roleService.update(role);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            roleService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/bind-resources")
    public Result<Void> bindResources(@RequestBody Map<String, Object> params) {
        try {
            String roleId = (String) params.get("roleId");
            @SuppressWarnings("unchecked")
            List<String> resourceIds = (List<String>) params.get("resourceIds");
            roleService.bindResources(roleId, resourceIds);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/bind-users")
    public Result<Void> bindUsers(@RequestBody Map<String, Object> params) {
        try {
            String roleId = (String) params.get("roleId");
            @SuppressWarnings("unchecked")
            List<String> userIds = (List<String>) params.get("userIds");
            roleService.bindUsers(roleId, userIds);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/bind-menus")
    public Result<Void> bindMenus(@RequestBody Map<String, Object> params) {
        try {
            String roleId = (String) params.get("roleId");
            @SuppressWarnings("unchecked")
            List<String> menuIds = (List<String>) params.get("menuIds");
            roleService.bindMenus(roleId, menuIds);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/resources/{roleId}")
    public Result<List<Resource>> getRoleResources(@PathVariable String roleId) {
        List<Resource> resources = roleService.getRoleResources(roleId);
        return Result.success(resources);
    }
}
