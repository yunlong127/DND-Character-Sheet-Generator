package com.dndhero.model.dto;

import java.util.List;
import java.util.Map;

/**
 * 生成统计数据
 */
public class GenerationStats {
    // 阵营统计: key=善良/中立/邪恶, value=Map<守序/中立/混乱/总和, count>
    private Map<String, Map<String, Integer>> alignmentStats;

    // 属性统计
    private List<AttrStats> attributeStats;

    // 职业统计
    private Map<String, Integer> careerStats;

    // 种族统计
    private Map<String, Integer> raceStats;

    public Map<String, Map<String, Integer>> getAlignmentStats() { return alignmentStats; }
    public void setAlignmentStats(Map<String, Map<String, Integer>> alignmentStats) { this.alignmentStats = alignmentStats; }
    public List<AttrStats> getAttributeStats() { return attributeStats; }
    public void setAttributeStats(List<AttrStats> attributeStats) { this.attributeStats = attributeStats; }
    public Map<String, Integer> getCareerStats() { return careerStats; }
    public void setCareerStats(Map<String, Integer> careerStats) { this.careerStats = careerStats; }
    public Map<String, Integer> getRaceStats() { return raceStats; }
    public void setRaceStats(Map<String, Integer> raceStats) { this.raceStats = raceStats; }

    public static class AttrStats {
        private String name;
        private double avg;
        private int max;
        private int min;
        public AttrStats() {}
        public AttrStats(String name, double avg, int max, int min) {
            this.name = name; this.avg = avg; this.max = max; this.min = min;
        }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getAvg() { return avg; }
        public void setAvg(double avg) { this.avg = avg; }
        public int getMax() { return max; }
        public void setMax(int max) { this.max = max; }
        public int getMin() { return min; }
        public void setMin(int min) { this.min = min; }
    }
}
