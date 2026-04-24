package com.example.permission.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class Resource implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String code;
    private String parentId;
    private Integer type;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private String permission;
    private String description;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<Api> apis = new ArrayList<>();
    private List<Resource> children = new ArrayList<>();
}
