package com.dndhero.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pray")
public class Pray {
    @Id
    private String name;

    private Boolean mo;
    private String ability;

    @Column(name = "`choose`")
    private String choose;

    private Boolean canSpell;
    private String information;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getMo() { return mo; }
    public void setMo(Boolean mo) { this.mo = mo; }
    public String getAbility() { return ability; }
    public void setAbility(String ability) { this.ability = ability; }
    public String getChoose() { return choose; }
    public void setChoose(String choose) { this.choose = choose; }
    public Boolean getCanSpell() { return canSpell; }
    public void setCanSpell(Boolean canSpell) { this.canSpell = canSpell; }
    public String getInformation() { return information; }
    public void setInformation(String information) { this.information = information; }
}
