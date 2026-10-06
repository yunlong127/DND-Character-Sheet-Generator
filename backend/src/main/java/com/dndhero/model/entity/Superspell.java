package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "superspell")
public class Superspell {
    @Id
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
