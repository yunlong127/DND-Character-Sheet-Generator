package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hero")
public class Hero {
    @Id
    private Integer id;

    private String name;
    private String sex;
    private String race;
    private String career;
    private String attribute;
    private Integer sum;
    private String camp;
    private String skill;
    private String tool;
    private String equip;
    private String language;
    private String feat;

    @Column(name = "`save`")
    private String save;

    private Integer money;
    private String spell0;
    private String spell1;
    private String spell2;
    private String pray;
    private String style;
    private String superspell;
    private String fight;
    private String other;
    private String age;
    private String live;

    private Boolean favorite;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getRace() { return race; }
    public void setRace(String race) { this.race = race; }
    public String getCareer() { return career; }
    public void setCareer(String career) { this.career = career; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public Integer getSum() { return sum; }
    public void setSum(Integer sum) { this.sum = sum; }
    public String getCamp() { return camp; }
    public void setCamp(String camp) { this.camp = camp; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }
    public String getEquip() { return equip; }
    public void setEquip(String equip) { this.equip = equip; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getFeat() { return feat; }
    public void setFeat(String feat) { this.feat = feat; }
    public String getSave() { return save; }
    public void setSave(String save) { this.save = save; }
    public Integer getMoney() { return money; }
    public void setMoney(Integer money) { this.money = money; }
    public String getSpell0() { return spell0; }
    public void setSpell0(String spell0) { this.spell0 = spell0; }
    public String getSpell1() { return spell1; }
    public void setSpell1(String spell1) { this.spell1 = spell1; }
    public String getSpell2() { return spell2; }
    public void setSpell2(String spell2) { this.spell2 = spell2; }
    public String getPray() { return pray; }
    public void setPray(String pray) { this.pray = pray; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String getSuperspell() { return superspell; }
    public void setSuperspell(String superspell) { this.superspell = superspell; }
    public String getFight() { return fight; }
    public void setFight(String fight) { this.fight = fight; }
    public String getOther() { return other; }
    public void setOther(String other) { this.other = other; }
    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }
    public String getLive() { return live; }
    public void setLive(String live) { this.live = live; }

    public Boolean getFavorite() { return favorite; }
    public void setFavorite(Boolean favorite) { this.favorite = favorite; }
}
