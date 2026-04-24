package com.example.permission.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String organizationId;
    private Integer type;
    private String description;
    private Integer status;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Organization organization;
}
