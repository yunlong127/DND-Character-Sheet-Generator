package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "spell")
public class Spell {
    @Id
    private String name;
    private Integer level;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}
