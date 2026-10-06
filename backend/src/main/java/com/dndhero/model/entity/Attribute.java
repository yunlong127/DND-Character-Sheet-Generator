package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attribute")
public class Attribute {
    @Id
    private String name;
    private Integer id;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}
