package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "style")
public class Style {
    @Id
    private String name;

    @Column(name = "`choose`")
    private String choose;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
}
