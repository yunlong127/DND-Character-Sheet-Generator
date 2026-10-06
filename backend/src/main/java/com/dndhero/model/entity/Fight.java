package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fight")
public class Fight {
    @Id
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
