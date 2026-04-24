package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Organization;
import com.example.permission.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/tree")
    public Result<List<Organization>> tree() {
        List<Organization> tree = organizationService.list();
        return Result.success(tree);
    }

    @GetMapping("/{id}")
    public Result<Organization> getById(@PathVariable String id) {
        Organization org = organizationService.getById(id);
        if (org == null) {
            return Result.error("组织机构不存在");
        }
        return Result.success(org);
    }

    @PostMapping
    public Result<Organization> create(@RequestBody Organization organization) {
        try {
            Organization created = organizationService.create(organization);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Organization> update(@RequestBody Organization organization) {
        try {
            Organization updated = organizationService.update(organization);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            organizationService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
