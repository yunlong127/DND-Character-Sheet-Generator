package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tool")
public class ToolTable {
    @Id
    private String name;           // varchar(20) PK
    private String type;           // varchar(30)
    private String before;         // varchar(30), pre-req
    private boolean improve;       // tinyint(1), not null
    private double value;          // double, not null
    private double weight;         // double, not null
    private String information;    // varchar(800)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public boolean isImprove() { return improve; }
    public void setImprove(boolean improve) { this.improve = improve; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }
}
