package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "language")
public class Language {
    @Id
    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
