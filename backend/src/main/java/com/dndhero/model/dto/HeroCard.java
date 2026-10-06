package com.dndhero.model.dto;

import java.util.List;

public class HeroCard {
    private Integer id;
    private String name;
    private String sex;
    private String race;
    private String career;
    private List<Integer> attributes;  // [str,dex,con,int,wis,cha]
    private Integer sum;
    private String camp;
    private List<String> skills;
    private List<String> tools;
    private List<String> equips;
    private List<String> languages;
    private List<String> feats;
    private String save;
    private Integer money;
    private List<String> spells0;
    private List<String> spells1;
    private List<String> spells2;
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
    public List<Integer> getAttributes() { return attributes; }
    public void setAttributes(List<Integer> attributes) { this.attributes = attributes; }
    public Integer getSum() { return sum; }
    public void setSum(Integer sum) { this.sum = sum; }
    public String getCamp() { return camp; }
    public void setCamp(String camp) { this.camp = camp; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public List<String> getTools() { return tools; }
    public void setTools(List<String> tools) { this.tools = tools; }
    public List<String> getEquips() { return equips; }
    public void setEquips(List<String> equips) { this.equips = equips; }
    public List<String> getLanguages() { return languages; }
    public void setLanguages(List<String> languages) { this.languages = languages; }
    public List<String> getFeats() { return feats; }
    public void setFeats(List<String> feats) { this.feats = feats; }
    public String getSave() { return save; }
    public void setSave(String save) { this.save = save; }
    public Integer getMoney() { return money; }
    public void setMoney(Integer money) { this.money = money; }
    public List<String> getSpells0() { return spells0; }
    public void setSpells0(List<String> spells0) { this.spells0 = spells0; }
    public List<String> getSpells1() { return spells1; }
    public void setSpells1(List<String> spells1) { this.spells1 = spells1; }
    public List<String> getSpells2() { return spells2; }
    public void setSpells2(List<String> spells2) { this.spells2 = spells2; }
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
