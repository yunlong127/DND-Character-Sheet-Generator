package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "information")
public class Information {
    @Id
    private String name;
    private String information;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }
}
