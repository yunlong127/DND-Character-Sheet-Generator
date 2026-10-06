package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "`save`")
public class SaveTable {
    @Id
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
