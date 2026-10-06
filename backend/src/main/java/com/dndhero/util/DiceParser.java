package com.dndhero.util;

import java.util.*;

/**
 * 骰子表达式解析器
 * adb: a次随机,每次在1~b之间,求和
 * adbdc: a次随机,每次在1~b之间,去掉最小c个,求和
 * adbdcde: a次随机,每次在1~b之间,去掉最小c个和最大e个,求和
 */
public class DiceParser {

    public static int roll(String expression) {
        expression = expression.trim().toLowerCase();
        // 匹配 adbdcde 格式
        if (expression.matches("\\d+d\\d+d\\d+d\\d+")) {
            return rollRemoveBoth(expression);
        }
        // 匹配 adbdc 格式
        if (expression.matches("\\d+d\\d+d\\d+")) {
            return rollRemoveMin(expression);
        }
        // 匹配 adb 格式
        if (expression.matches("\\d+d\\d+")) {
            return rollSimple(expression);
        }
        throw new IllegalArgumentException("无法解析的骰子表达式: " + expression);
    }

    private static int rollSimple(String expr) {
        String[] parts = expr.split("d");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int sum = 0;
        Random rng = new Random();
        for (int i = 0; i < a; i++) {
            sum += rng.nextInt(b) + 1;
        }
        return sum;
    }

    private static int rollRemoveMin(String expr) {
        String[] parts = expr.split("d");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        return rollRemove(a, b, c, 0);
    }

    private static int rollRemoveBoth(String expr) {
        String[] parts = expr.split("d");
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = Integer.parseInt(parts[2]);
        int e = Integer.parseInt(parts[3]);
        return rollRemove(a, b, c, e);
    }

    private static int rollRemove(int count, int sides, int removeMin, int removeMax) {
        List<Integer> results = new ArrayList<>();
        Random rng = new Random();
        for (int i = 0; i < count; i++) {
            results.add(rng.nextInt(sides) + 1);
        }
        Collections.sort(results);
        // 去掉最小的 removeMin 个
        int removeCount = Math.min(removeMin + removeMax, results.size() - 1);
        if (removeMin > 0 && removeMin <= removeCount) {
            results = new ArrayList<>(results.subList(removeMin, results.size()));
            removeCount -= removeMin;
        }
        // 去掉最大的 removeMax 个
        if (removeMax > 0 && removeMax <= results.size() - 1) {
            results = new ArrayList<>(results.subList(0, results.size() - removeMax));
        }
        return results.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * 解析职业的money表达式: a,b,c 计算 adb*c
     */
    public static int rollMoney(String moneyExpr) {
        String[] parts = moneyExpr.split(",");
        if (parts.length != 3) {
            return 0;
        }
        int a = Integer.parseInt(parts[0].trim());
        int b = Integer.parseInt(parts[1].trim());
        int c = Integer.parseInt(parts[2].trim());
        return rollSimple(a + "d" + b) * c;
    }

    /**
     * Get min possible value for a dice expression (adb / adbdc / adbdcde)
     */
    public static int minVal(String expression) {
        expression = expression.trim().toLowerCase();
        String[] parts = expression.split("d");
        if (parts.length < 2) return 0;
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
        int e = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0;
        int keep = a - c - e;
        return Math.max(1, keep);
    }

    /**
     * Get max possible value for a dice expression
     */
    public static int maxVal(String expression) {
        expression = expression.trim().toLowerCase();
        String[] parts = expression.split("d");
        if (parts.length < 2) return 0;
        int a = Integer.parseInt(parts[0]);
        int b = Integer.parseInt(parts[1]);
        int c = parts.length >= 3 ? Integer.parseInt(parts[2]) : 0;
        int e = parts.length >= 4 ? Integer.parseInt(parts[3]) : 0;
        int keep = a - c - e;
        return keep * b;
    }
}
