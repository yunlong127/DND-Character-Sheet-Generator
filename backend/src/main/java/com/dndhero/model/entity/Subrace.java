package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subrace")
public class Subrace {
    @Id
    private String name;
    private String attribute;
    private String camp;
    private String ability;

    @Column(name = "`choose`")
    private String choose;

    private String skill;
    private Boolean canSpell;
    private String age;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public String getCamp() { return camp; }
    public void setCamp(String camp) { this.camp = camp; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public Boolean getCanSpell() { return canSpell; }
    public void setCanSpell(Boolean canSpell) { this.canSpell = canSpell; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
}
