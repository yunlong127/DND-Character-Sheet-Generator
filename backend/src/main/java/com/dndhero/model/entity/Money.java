package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "money")
public class Money {
    @Id
    private String name;
    private String cost;
    private Integer init;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCost() { return cost; }
    public void setCost(String cost) { this.cost = cost; }
    public Integer getInit() { return init; }
    public void setInit(Integer init) { this.init = init; }
}
