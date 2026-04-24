package com.example.permission.store;

import cn.hutool.core.util.IdUtil;
import com.example.permission.entity.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final Map<String, User> users = new ConcurrentHashMap<>();
    private static final Map<String, Organization> organizations = new ConcurrentHashMap<>();
    private static final Map<String, Role> roles = new ConcurrentHashMap<>();
    private static final Map<String, Resource> resources = new ConcurrentHashMap<>();
    private static final Map<String, Api> apis = new ConcurrentHashMap<>();
    private static final Map<String, Application> applications = new ConcurrentHashMap<>();

    public static void init() {
        String orgId1 = IdUtil.simpleUUID();
        String orgId2 = IdUtil.simpleUUID();
        String orgId3 = IdUtil.simpleUUID();

        Organization org1 = new Organization();
        org1.setId(orgId1);
        org1.setName("总公司");
        org1.setCode("COMPANY_ROOT");
        org1.setParentId("0");
        org1.setLevel(1);
        org1.setSort(1);
        org1.setStatus(1);
        org1.setIsDeleted(0);
        org1.setCreateTime(LocalDateTime.now());
        org1.setUpdateTime(LocalDateTime.now());
        organizations.put(orgId1, org1);

        Organization org2 = new Organization();
        org2.setId(orgId2);
        org2.setName("技术部");
        org2.setCode("TECH_DEPT");
        org2.setParentId(orgId1);
        org2.setLevel(2);
        org2.setSort(1);
        org2.setStatus(1);
        org2.setIsDeleted(0);
        org2.setCreateTime(LocalDateTime.now());
        org2.setUpdateTime(LocalDateTime.now());
        organizations.put(orgId2, org2);

        Organization org3 = new Organization();
        org3.setId(orgId3);
        org3.setName("市场部");
        org3.setCode("MARKET_DEPT");
        org3.setParentId(orgId1);
        org3.setLevel(2);
        org3.setSort(2);
        org3.setStatus(1);
        org3.setIsDeleted(0);
        org3.setCreateTime(LocalDateTime.now());
        org3.setUpdateTime(LocalDateTime.now());
        organizations.put(orgId3, org3);

        String adminRoleId = IdUtil.simpleUUID();
        String userRoleId = IdUtil.simpleUUID();

        Role adminRole = new Role();
        adminRole.setId(adminRoleId);
        adminRole.setName("超级管理员");
        adminRole.setCode("SUPER_ADMIN");
        adminRole.setDescription("拥有所有权限");
        adminRole.setOrganizationId(orgId1);
        adminRole.setStatus(1);
        adminRole.setIsDeleted(0);
        adminRole.setCreateTime(LocalDateTime.now());
        adminRole.setUpdateTime(LocalDateTime.now());
        roles.put(adminRoleId, adminRole);

        Role userRole = new Role();
        userRole.setId(userRoleId);
        userRole.setName("普通用户");
        userRole.setCode("COMMON_USER");
        userRole.setDescription("普通用户角色");
        userRole.setOrganizationId(orgId2);
        userRole.setStatus(1);
        userRole.setIsDeleted(0);
        userRole.setCreateTime(LocalDateTime.now());
        userRole.setUpdateTime(LocalDateTime.now());
        roles.put(userRoleId, userRole);

        String menu1Id = IdUtil.simpleUUID();
        String menu2Id = IdUtil.simpleUUID();
        String menu3Id = IdUtil.simpleUUID();
        String menu4Id = IdUtil.simpleUUID();
        String menu5Id = IdUtil.simpleUUID();
        String menu6Id = IdUtil.simpleUUID();

        Resource menu1 = new Resource();
        menu1.setId(menu1Id);
        menu1.setName("系统管理");
        menu1.setCode("SYSTEM_MANAGE");
        menu1.setParentId("0");
        menu1.setType(0);
        menu1.setPath("/system");
        menu1.setIcon("Setting");
        menu1.setSort(1);
        menu1.setStatus(1);
        menu1.setIsDeleted(0);
        menu1.setCreateTime(LocalDateTime.now());
        menu1.setUpdateTime(LocalDateTime.now());
        resources.put(menu1Id, menu1);

        Resource menu2 = new Resource();
        menu2.setId(menu2Id);
        menu2.setName("用户管理");
        menu2.setCode("USER_MANAGE");
        menu2.setParentId(menu1Id);
        menu2.setType(1);
        menu2.setPath("/system/user");
        menu2.setComponent("UserManagement");
        menu2.setSort(1);
        menu2.setStatus(1);
        menu2.setIsDeleted(0);
        menu2.setCreateTime(LocalDateTime.now());
        menu2.setUpdateTime(LocalDateTime.now());
        resources.put(menu2Id, menu2);

        Resource menu3 = new Resource();
        menu3.setId(menu3Id);
        menu3.setName("角色管理");
        menu3.setCode("ROLE_MANAGE");
        menu3.setParentId(menu1Id);
        menu3.setType(1);
        menu3.setPath("/system/role");
        menu3.setComponent("RoleManagement");
        menu3.setSort(2);
        menu3.setStatus(1);
        menu3.setIsDeleted(0);
        menu3.setCreateTime(LocalDateTime.now());
        menu3.setUpdateTime(LocalDateTime.now());
        resources.put(menu3Id, menu3);

        Resource menu4 = new Resource();
        menu4.setId(menu4Id);
        menu4.setName("组织机构");
        menu4.setCode("ORG_MANAGE");
        menu4.setParentId(menu1Id);
        menu4.setType(1);
        menu4.setPath("/system/organization");
        menu4.setComponent("OrganizationManagement");
        menu4.setSort(3);
        menu4.setStatus(1);
        menu4.setIsDeleted(0);
        menu4.setCreateTime(LocalDateTime.now());
        menu4.setUpdateTime(LocalDateTime.now());
        resources.put(menu4Id, menu4);

        Resource menu5 = new Resource();
        menu5.setId(menu5Id);
        menu5.setName("资源管理");
        menu5.setCode("RESOURCE_MANAGE");
        menu5.setParentId(menu1Id);
        menu5.setType(1);
        menu5.setPath("/system/resource");
        menu5.setComponent("ResourceManagement");
        menu5.setSort(4);
        menu5.setStatus(1);
        menu5.setIsDeleted(0);
        menu5.setCreateTime(LocalDateTime.now());
        menu5.setUpdateTime(LocalDateTime.now());
        resources.put(menu5Id, menu5);

        Resource menu6 = new Resource();
        menu6.setId(menu6Id);
        menu6.setName("应用管理");
        menu6.setCode("APP_MANAGE");
        menu6.setParentId("0");
        menu6.setType(0);
        menu6.setPath("/app");
        menu6.setIcon("Grid");
        menu6.setSort(2);
        menu6.setStatus(1);
        menu6.setIsDeleted(0);
        menu6.setCreateTime(LocalDateTime.now());
        menu6.setUpdateTime(LocalDateTime.now());
        resources.put(menu6Id, menu6);

        List<String> adminResourceIds = new ArrayList<>();
        adminResourceIds.add(menu1Id);
        adminResourceIds.add(menu2Id);
        adminResourceIds.add(menu3Id);
        adminResourceIds.add(menu4Id);
        adminResourceIds.add(menu5Id);
        adminResourceIds.add(menu6Id);
        adminRole.setResourceIds(adminResourceIds);

        List<String> userResourceIds = new ArrayList<>();
        userResourceIds.add(menu1Id);
        userResourceIds.add(menu2Id);
        userRole.setResourceIds(userResourceIds);

        String userId1 = IdUtil.simpleUUID();
        String userId2 = IdUtil.simpleUUID();

        User admin = new User();
        admin.setId(userId1);
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRealName("管理员");
        admin.setEmail("admin@example.com");
        admin.setPhone("13800138000");
        admin.setOrganizationId(orgId1);
        admin.setStatus(1);
        admin.setIsDeleted(0);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        admin.getRoleIds().add(adminRoleId);
        users.put(userId1, admin);

        User user = new User();
        user.setId(userId2);
        user.setUsername("user");
        user.setPassword("user123");
        user.setRealName("普通用户");
        user.setEmail("user@example.com");
        user.setPhone("13800138001");
        user.setOrganizationId(orgId2);
        user.setStatus(1);
        user.setIsDeleted(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.getRoleIds().add(userRoleId);
        users.put(userId2, user);

        adminRole.getUserIds().add(userId1);
        userRole.getUserIds().add(userId2);

        String appId1 = IdUtil.simpleUUID();
        String appId2 = IdUtil.simpleUUID();

        Application app1 = new Application();
        app1.setId(appId1);
        app1.setName("ERP系统");
        app1.setOrganizationId(orgId1);
        app1.setType(0);
        app1.setDescription("企业资源计划系统");
        app1.setStatus(1);
        app1.setIsDeleted(0);
        app1.setCreateTime(LocalDateTime.now());
        app1.setUpdateTime(LocalDateTime.now());
        applications.put(appId1, app1);

        Application app2 = new Application();
        app2.setId(appId2);
        app2.setName("CRM系统");
        app2.setOrganizationId(orgId2);
        app2.setType(1);
        app2.setDescription("客户关系管理系统");
        app2.setStatus(1);
        app2.setIsDeleted(0);
        app2.setCreateTime(LocalDateTime.now());
        app2.setUpdateTime(LocalDateTime.now());
        applications.put(appId2, app2);
    }

    public static Map<String, User> getUsers() {
        return users;
    }

    public static Map<String, Organization> getOrganizations() {
        return organizations;
    }

    public static Map<String, Role> getRoles() {
        return roles;
    }

    public static Map<String, Resource> getResources() {
        return resources;
    }

    public static Map<String, Api> getApis() {
        return apis;
    }

    public static Map<String, Application> getApplications() {
        return applications;
    }
}
