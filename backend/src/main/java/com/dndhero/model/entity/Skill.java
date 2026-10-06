package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill")
public class Skill {
    @Id
    private String name;       // varchar(8) PK
    private int attribute;     // int, not null
    private String before;     // varchar(30), pre-req for 专精
    private boolean improve;   // tinyint(1), not null

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAttribute() { return attribute; }
    public void setAttribute(int attribute) { this.attribute = attribute; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public boolean isImprove() { return improve; }
    public void setImprove(boolean improve) { this.improve = improve; }
}
