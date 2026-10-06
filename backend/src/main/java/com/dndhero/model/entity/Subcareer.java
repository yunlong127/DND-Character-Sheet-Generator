package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subcareer")
public class Subcareer {
    @Id
    private String name;
    private String camp;

    @Column(name = "`choose`")
    private String choose;

    private String ability;
    private String skill;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCamp() { return camp; }
    public void setCamp(String camp) { this.camp = camp; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
}
