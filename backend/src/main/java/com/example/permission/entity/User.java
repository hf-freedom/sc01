package com.example.permission.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String password;
    private String realName;
    private String email;
    private String phone;
    private String organizationId;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<String> roleIds = new ArrayList<>();
    private List<Role> roles = new ArrayList<>();
    private Organization organization;
    private List<Resource> menus = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
}
