package com.example.permission.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String code;
    private String description;
    private String organizationId;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<String> resourceIds = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private List<String> menuIds = new ArrayList<>();
    
    private List<Resource> resources = new ArrayList<>();
    private List<User> users = new ArrayList<>();
}
