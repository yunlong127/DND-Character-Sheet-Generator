package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "race")
public class Race {
    @Id
    private String name;
    private Integer size;
    private String ability;

    @Column(name = "`choose`")
    private String choose;

    private String skill;
    private String sub;
    private String raceName;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getRaceName() { return raceName; }
    public void setRaceName(String raceName) { this.raceName = raceName; }
}
