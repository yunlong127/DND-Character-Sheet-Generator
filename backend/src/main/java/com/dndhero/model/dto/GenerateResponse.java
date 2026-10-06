package com.dndhero.model.dto;

import java.util.List;

public class GenerateResponse {
    private boolean success;
    private String message;
    private HeroCard hero;
    private Integer heroId;
    private List<HeroCard> heroes;          // list for random generation
    private GenerationStats stats;           // statistics
    private List<String> availableCareers;  // for manual mode
    private List<String> availableRaces;    // for manual mode

    // ... existing getters/setters ...

    public GenerationStats getStats() { return stats; }
    public void setStats(GenerationStats stats) { this.stats = stats; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public HeroCard getHero() { return hero; }
    public void setHero(HeroCard hero) { this.hero = hero; }
    public Integer getHeroId() { return heroId; }
    public void setHeroId(Integer heroId) { this.heroId = heroId; }
    public List<HeroCard> getHeroes() { return heroes; }
    public void setHeroes(List<HeroCard> heroes) { this.heroes = heroes; }
    public List<String> getAvailableCareers() { return availableCareers; }
    public void setAvailableCareers(List<String> availableCareers) { this.availableCareers = availableCareers; }
    public List<String> getAvailableRaces() { return availableRaces; }
    public void setAvailableRaces(List<String> availableRaces) { this.availableRaces = availableRaces; }
}
