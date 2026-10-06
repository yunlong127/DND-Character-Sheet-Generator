package com.dndhero.model.dto;

import java.util.List;
import java.util.Map;

/**
 * Wizard step response: shows current step title and filtered candidates.
 */
public class WizardStepResponse {
    private String sessionId;
    private String stepTitle;        // e.g. "选择种族", "选择技能", "专长-幸运", "装备-轻甲"
    private String stepType;         // "pick_race", "pick_subrace", "pick_choose", "final"
    private String description;      // 该步骤描述
    private List<String> candidates; // 过滤后的可选项列表
    private int selectCount;         // 需要选择的数量（cnt），0表示用户需从candidates中选这么多
    private boolean allowSkip;       // 是否允许跳过
    private HeroCard preview;        // 最终时返回预览
    private List<String> previewContent; // 当前已选内容
    private int[] rolledAttributes;  // 随机生成的属性
    private int attrsSum;            // 属性总和
    private List<List<Integer>> attrsOptions; // 多组可选属性（属性选择步骤）
    private Integer ageMin;            // 年龄下限
    private Integer ageMax;            // 年龄上限

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getStepTitle() { return stepTitle; }
    public void setStepTitle(String stepTitle) { this.stepTitle = stepTitle; }
    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<String> getCandidates() { return candidates; }
    public void setCandidates(List<String> candidates) { this.candidates = candidates; }
    public int getSelectCount() { return selectCount; }
    public void setSelectCount(int selectCount) { this.selectCount = selectCount; }
    public boolean isAllowSkip() { return allowSkip; }
    public void setAllowSkip(boolean allowSkip) { this.allowSkip = allowSkip; }
    public HeroCard getPreview() { return preview; }
    public void setPreview(HeroCard preview) { this.preview = preview; }
    public List<String> getPreviewContent() { return previewContent; }
    public void setPreviewContent(List<String> previewContent) { this.previewContent = previewContent; }
    public int[] getRolledAttributes() { return rolledAttributes; }
    public void setRolledAttributes(int[] rolledAttributes) { this.rolledAttributes = rolledAttributes; }
    public int getAttrsSum() { return attrsSum; }
    public void setAttrsSum(int attrsSum) { this.attrsSum = attrsSum; }
    public List<List<Integer>> getAttrsOptions() { return attrsOptions; }
    public void setAttrsOptions(List<List<Integer>> attrsOptions) { this.attrsOptions = attrsOptions; }
    public Integer getAgeMin() { return ageMin; }
    public void setAgeMin(Integer ageMin) { this.ageMin = ageMin; }
    public Integer getAgeMax() { return ageMax; }
    public void setAgeMax(Integer ageMax) { this.ageMax = ageMax; }
}
