package com.dndhero.model.dto;

import java.util.List;

public class GenerateRequest {
    private String formula = "5d6d1d1";
    private String background = "背景-强化";
    private int count = 1;
    private Integer rollCount;
    private String mode; // "roll"(default) or "input"
    private String sex;
    private String raceName;
    private String careerName;
    private String subraceName;
    private String subcareerName;
    private List<Integer> manualAttributes;

    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public Integer getRollCount() { return rollCount; }
    public void setRollCount(Integer rollCount) { this.rollCount = rollCount; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getRaceName() { return raceName; }
    public void setRaceName(String raceName) { this.raceName = raceName; }
    public String getCareerName() { return careerName; }
    public void setCareerName(String careerName) { this.careerName = careerName; }
    public String getSubraceName() { return subraceName; }
    public void setSubraceName(String subraceName) { this.subraceName = subraceName; }
    public String getSubcareerName() { return subcareerName; }
    public void setSubcareerName(String subcareerName) { this.subcareerName = subcareerName; }
    public List<Integer> getManualAttributes() { return manualAttributes; }
    public void setManualAttributes(List<Integer> manualAttributes) { this.manualAttributes = manualAttributes; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}
