package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "career")
public class Career {
    @Id
    private String name;
    private String camp;
    private String before;
    private String bchoose;
    private String ability;

    @Column(name = "`choose`")
    private String choose;

    private Boolean canSpell;
    private String skill;
    private String money;
    private String sub;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCamp() { return camp; }
    public void setCamp(String camp) { this.camp = camp; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public String getBchoose() { return bchoose; }
    public void setBchoose(String bchoose) { this.bchoose = bchoose; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public Boolean getCanSpell() { return canSpell; }
    public void setCanSpell(Boolean canSpell) { this.canSpell = canSpell; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getMoney() { return money; }
    public void setMoney(String money) { this.money = money; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
}
