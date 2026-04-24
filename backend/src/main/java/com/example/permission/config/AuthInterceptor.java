package com.example.permission.config;

import cn.hutool.core.util.StrUtil;
import com.example.permission.common.Result;
import com.example.permission.context.UserContext;
import com.example.permission.entity.*;
import com.example.permission.store.DataStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AuthInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String userId = request.getHeader("X-User-Id");
        if (StrUtil.isBlank(userId)) {
            writeResponse(response, Result.unauthorized());
            return false;
        }

        User user = DataStore.getUsers().get(userId);
        if (user == null || user.getStatus() != 1 || user.getIsDeleted() == 1) {
            writeResponse(response, Result.unauthorized());
            return false;
        }

        User enrichedUser = enrichUserInfo(user);
        UserContext.setCurrentUser(enrichedUser);

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (!hasApiPermission(enrichedUser, requestUri, requestMethod)) {
            boolean hasAnyApiBindings = checkHasAnyApiBindings();
            if (hasAnyApiBindings) {
                writeResponse(response, Result.forbidden());
                return false;
            }
        }

        return true;
    }

    private User enrichUserInfo(User user) {
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
        result.setRoleIds(user.getRoleIds() != null ? user.getRoleIds() : new java.util.ArrayList<>());

        if (StrUtil.isNotBlank(user.getOrganizationId())) {
            Organization org = DataStore.getOrganizations().get(user.getOrganizationId());
            if (org != null && org.getIsDeleted() == 0) {
                result.setOrganization(org);
            }
        }

        List<Role> roles = new java.util.ArrayList<>();
        Set<String> allResourceIds = new HashSet<>();
        for (String roleId : user.getRoleIds()) {
            Role role = DataStore.getRoles().get(roleId);
            if (role != null && role.getStatus() == 1 && role.getIsDeleted() == 0) {
                roles.add(role);
                if (role.getResourceIds() != null) {
                    allResourceIds.addAll(role.getResourceIds());
                }
            }
        }
        result.setRoles(roles);

        List<Resource> allResources = new java.util.ArrayList<>();
        Set<String> permissions = new HashSet<>();
        for (String resourceId : allResourceIds) {
            Resource resource = DataStore.getResources().get(resourceId);
            if (resource != null && resource.getStatus() == 1 && resource.getIsDeleted() == 0) {
                allResources.add(resource);
                if (StrUtil.isNotBlank(resource.getPermission())) {
                    permissions.add(resource.getPermission());
                }
            }
        }

        List<Resource> menuTree = buildResourceTree(allResources);
        result.setMenus(menuTree);
        result.setPermissions(new java.util.ArrayList<>(permissions));

        return result;
    }

    private List<Resource> buildResourceTree(List<Resource> allResources) {
        if (allResources == null || allResources.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        Map<String, List<Resource>> parentMap = allResources.stream()
                .collect(java.util.stream.Collectors.groupingBy(Resource::getParentId));

        for (Resource resource : allResources) {
            resource.setChildren(parentMap.getOrDefault(resource.getId(), new java.util.ArrayList<>()));
        }

        return allResources.stream()
                .filter(resource -> "0".equals(resource.getParentId()))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean hasApiPermission(User user, String requestUri, String requestMethod) {
        if (user.getPermissions() == null) {
            return false;
        }

        Set<String> allResourceIds = new HashSet<>();
        for (Role role : user.getRoles()) {
            if (role.getStatus() == 1) {
                if (role.getResourceIds() != null) {
                    allResourceIds.addAll(role.getResourceIds());
                }
            }
        }

        for (String resourceId : allResourceIds) {
            Resource resource = DataStore.getResources().get(resourceId);
            if (resource == null || resource.getStatus() != 1 || resource.getIsDeleted() == 1) {
                continue;
            }

            List<Api> apis = resource.getApis();
            if (apis == null) {
                continue;
            }

            for (Api api : apis) {
                if (api.getStatus() != 1 || api.getIsDeleted() == 1) {
                    continue;
                }
                if (matchUri(requestUri, api.getUrl()) && requestMethod.equalsIgnoreCase(api.getMethod())) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchUri(String requestUri, String apiUrl) {
        if (apiUrl == null) {
            return false;
        }
        if (apiUrl.contains("{id}")) {
            String pattern = apiUrl.replace("{id}", "[^/]+");
            return requestUri.matches(pattern);
        }
        return requestUri.equals(apiUrl);
    }

    private boolean checkHasAnyApiBindings() {
        for (Resource resource : DataStore.getResources().values()) {
            if (resource.getApis() != null && !resource.getApis().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeResponse(HttpServletResponse response, Result<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
