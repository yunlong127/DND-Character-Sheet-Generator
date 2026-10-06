package com.dndhero.model.dto;

import java.util.List;

/**
 * User's choice in wizard step: sessionId + picked items
 */
public class WizardChoiceRequest {
    private String sessionId;
    private List<String> selected;  // user's chosen items from candidates

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public List<String> getSelected() { return selected; }
    public void setSelected(List<String> selected) { this.selected = selected; }
}
