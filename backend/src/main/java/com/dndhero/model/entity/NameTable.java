package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "name")
public class NameTable {
    @Id
    private String name;
    private String manName;
    private String womanName;
    private String lname;
    private String alias;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getManName() { return manName; }
    public void setManName(String manName) { this.manName = manName; }
    public String getWomanName() { return womanName; }
    public void setWomanName(String womanName) { this.womanName = womanName; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
}
