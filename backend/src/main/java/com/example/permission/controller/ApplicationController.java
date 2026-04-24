package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.Application;
import com.example.permission.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/list")
    public Result<List<Application>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String organizationId) {
        List<Application> applications = applicationService.list(keyword, type, organizationId);
        return Result.success(applications);
    }

    @GetMapping("/{id}")
    public Result<Application> getById(@PathVariable String id) {
        Application app = applicationService.getById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        return Result.success(app);
    }

    @PostMapping
    public Result<Application> create(@RequestBody Application application) {
        try {
            Application created = applicationService.create(application);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Application> update(@RequestBody Application application) {
        try {
            Application updated = applicationService.update(application);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            applicationService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/toggle/{id}")
    public Result<Void> toggleStatus(@PathVariable String id) {
        try {
            applicationService.toggleStatus(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
