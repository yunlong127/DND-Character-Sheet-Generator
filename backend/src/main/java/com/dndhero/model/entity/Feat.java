package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "feat")
public class Feat {
    @Id
    private String name;
    private String bchoose;
    private Boolean bspell;
    private String before;

    @Column(name = "`choose`")
    private String choose;

    private String ability;
    private Boolean canSpell;
    private String information;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBchoose() { return bchoose; }
    public void setBchoose(String bchoose) { this.bchoose = bchoose; }
    public Boolean getBspell() { return bspell; }
    public void setBspell(Boolean bspell) { this.bspell = bspell; }
    public String getBefore() { return before; }
    public void setBefore(String before) { this.before = before; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public Boolean getCanSpell() { return canSpell; }
    public void setCanSpell(Boolean canSpell) { this.canSpell = canSpell; }
    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }
}
