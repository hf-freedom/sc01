package com.example.permission.controller;

import com.example.permission.common.Result;
import com.example.permission.entity.User;
import com.example.permission.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userService.login(username, password);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String organizationId) {
        List<User> users = userService.list(keyword, organizationId);
        return Result.success(users);
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable String id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<User> create(@RequestBody User user) {
        try {
            User created = userService.create(user);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<User> update(@RequestBody User user) {
        try {
            User updated = userService.update(user);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        try {
            userService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/toggle/{id}")
    public Result<Void> toggleStatus(@PathVariable String id) {
        try {
            userService.toggleStatus(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
