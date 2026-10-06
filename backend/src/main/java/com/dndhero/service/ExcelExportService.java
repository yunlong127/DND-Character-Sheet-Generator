package com.dndhero.service;

import com.dndhero.model.entity.Hero;
import com.dndhero.repository.HeroRepository;
import com.dndhero.repository.SpellRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    private final HeroRepository heroRepo;
    private final Map<String, Integer> spellLevelMap;

    public ExcelExportService(HeroRepository heroRepo, SpellRepository spellRepo) {
        this.heroRepo = heroRepo;
        this.spellLevelMap = spellRepo.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(s -> s.getName(), s -> s.getLevel()));
    }

    public byte[] exportHero(Integer heroId) {
        Hero h = heroRepo.findById(heroId).orElse(null);
        if (h == null) return null;

        try (Workbook wb = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle labelStyle = createLabelStyle(wb);
            CellStyle valueStyle = createValueStyle(wb);
            CellStyle blankStyle = createBlankStyle(wb);
            CellStyle sectionStyle = createSectionStyle(wb);

            Sheet sheet = wb.createSheet("角色卡");
            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 14 * 256);
            sheet.setColumnWidth(2, 12 * 256);
            sheet.setColumnWidth(3, 14 * 256);
            sheet.setColumnWidth(4, 12 * 256);
            sheet.setColumnWidth(5, 14 * 256);
            sheet.setColumnWidth(6, 12 * 256);
            sheet.setColumnWidth(7, 14 * 256);

            int r = 0;

            // ===== Header =====
            r = writeRow(sheet, r, new String[]{"姓名", "性别", "种族", "阵营", "等级", "经验", "熟练加值", "激励点"}, labelStyle);
            r = writeRow(sheet, r,
                    new String[]{nvl(h.getName()), nvl(h.getSex()), nvl(h.getRace()), nvl(h.getCamp()), "1", "0", "2", ""},
                    valueStyle);

            // ===== Career (horizontal for multi-class) =====
            String[] careers = parseCareerList(h.getCareer());
            r = writeRow(sheet, r, new String[]{"职业1", "职业2", "职业3"}, labelStyle);
            {
                Row carRow = sheet.createRow(r++);
                for (int i = 0; i < 3; i++) {
                    createCell(carRow, i, i < careers.length ? careers[i] : "", valueStyle);
                }
            }
            r = writeRow(sheet, r, new String[]{"职业1等级", "职业2等级", "职业3等级"}, labelStyle);
            {
                Row lvlRow = sheet.createRow(r++);
                for (int i = 0; i < 3; i++) {
                    createCell(lvlRow, i, i < careers.length ? "1" : "", blankStyle);
                }
            }
            r = writeRow(sheet, r, new String[]{"职业1生命骰", "职业2生命骰", "职业3生命骰"}, labelStyle);
            {
                Row hdRow = sheet.createRow(r++);
                for (int i = 0; i < 3; i++) {
                    createCell(hdRow, i, "", blankStyle);
                }
            }

            r++; // blank row

            // ===== Career spell table (法术位) =====
            // Gather all hero items; find those with "-法表" suffix, look up spell level, group by环
            Set<String> allHeroItems = new LinkedHashSet<>();
            allHeroItems.addAll(parseList(h.getSpell0()));
            allHeroItems.addAll(parseList(h.getSpell1()));
            allHeroItems.addAll(parseList(h.getSpell2()));
            allHeroItems.addAll(parseList(h.getOther()));
            Map<Integer, List<String>> careerSpellSlots = new LinkedHashMap<>();
            for (int lvl = 1; lvl <= 9; lvl++) careerSpellSlots.put(lvl, new ArrayList<>());
            for (String item : allHeroItems) {
                if (item.endsWith("-法表")) {
                    String raw = item.substring(0, item.length() - 3);
                    Integer lvl = spellLevelMap.get(raw);
                    if (lvl != null && lvl >= 1 && lvl <= 9) {
                        careerSpellSlots.get(lvl).add(raw);
                    }
                }
            }
            r = writeRow(sheet, r, new String[]{"职业法术"}, sectionStyle);
            r = writeRow(sheet, r, new String[]{"准备数量", ""}, blankStyle);
            r = writeRow(sheet, r, new String[]{"准备法术", ""}, blankStyle);
            for (int lvl = 1; lvl <= 9; lvl++) {
                List<String> spells = careerSpellSlots.get(lvl);
                String val = spells.isEmpty() ? "" : String.join(", ", spells);
                r = writeRow(sheet, r, new String[]{lvl + "环法术位", val}, spells.isEmpty() ? blankStyle : valueStyle);
            }

            r++; // blank row

            // ===== Attributes =====
            int[] attrs = parseAttrs(h.getAttribute());
            String[] attrNames = {"力量", "敏捷", "体质", "智力", "感知", "魅力"};
            r = writeRow(sheet, r, new String[]{"属性", attrNames[0], attrNames[1], attrNames[2], attrNames[3], attrNames[4], attrNames[5], "总和"}, labelStyle);
            {
                Row row = sheet.createRow(r++);
                createCell(row, 0, "数值", valueStyle);
                for (int i = 0; i < 6; i++) createCell(row, i + 1, String.valueOf(attrs[i]), valueStyle);
                createCell(row, 7, String.valueOf(nvl(h.getSum())), valueStyle);
            }
            {
                Row row = sheet.createRow(r++);
                createCell(row, 0, "调整值", labelStyle);
                for (int i = 0; i < 6; i++) {
                    int mod = Math.floorDiv(attrs[i], 2) - 5;
                    createCell(row, i + 1, String.valueOf(mod), valueStyle);
                }
                createCell(row, 7, "", blankStyle);
            }

            r++; // blank row

            // ===== Combat stats =====
            String combatLabels[] = {"HP", "先攻", "AC", "负重", "最大负重"};
            Row combatLbl = sheet.createRow(r++);
            for (int i = 0; i < combatLabels.length; i++) createCell(combatLbl, i + 1, combatLabels[i], labelStyle);
            Row combatVal = sheet.createRow(r++);
            for (int i = 0; i < combatLabels.length; i++) createCell(combatVal, i + 1, "", blankStyle);

            r++; // blank row

            // ===== Skills & Equipment side by side =====
            Set<String> heroSkills = new HashSet<>(parseList(h.getSkill()));
            String[] skillNames = {"体操","医疗","历史","奥秘","威胁","宗教",
                                   "察觉","巧手","欺瞒","洞悉","生存","自然",
                                   "表演","说服","调查","运动","隐匿","驯兽"};
            String[] skillAttrs = {"敏捷","感知","智力","智力","魅力","智力",
                                   "感知","敏捷","魅力","感知","感知","智力",
                                   "魅力","魅力","智力","力量","敏捷","感知"};
            String[] equipSlots = {"装备位置","头","上身","左手","右手","下身","脚","挂载"};
            r = writeRow(sheet, r, new String[]{"技能", "数值", "", "装备", "数值"}, labelStyle);
            int maxRows = Math.max(skillNames.length, equipSlots.length);
            for (int i = 0; i < maxRows; i++) {
                Row row = sheet.createRow(r++);
                // Skills (cols 0-1)
                if (i < skillNames.length) {
                    String sn = skillNames[i];
                    boolean hasBase = heroSkills.contains(sn);
                    boolean hasPlus = heroSkills.contains(sn + "+");
                    int plusCount = (hasBase ? 1 : 0) + (hasPlus ? 1 : 0);
                    String display;
                    if (plusCount == 2) display = sn + "++";
                    else if (plusCount == 1) display = sn + "+";
                    else display = sn;
                    createCell(row, 0, display, plusCount > 0 ? valueStyle : blankStyle);
                    // Skill value = attr modifier + 2 * plusCount
                    int skillVal = attrMod(skillAttrs[i], attrs) + 2 * plusCount;
                    String sig = skillVal >= 0 ? "+" : "";
                    createCell(row, 1, sig + skillVal, valueStyle);
                } else {
                    createCell(row, 1, "", blankStyle);
                }
                // Equipment slots (cols 3-4)
                if (i < equipSlots.length) {
                    createCell(row, 3, equipSlots[i], i == 0 ? labelStyle : blankStyle);
                }
                createCell(row, 4, "", blankStyle);
            }

            r++; // blank row

            // ===== Languages, Tools, Saves, Equips =====
            r = writeRow(sheet, r, new String[]{"语言", nvl(h.getLanguage())}, valueStyle);
            r = writeRow(sheet, r, new String[]{"工具", nvl(h.getTool())}, valueStyle);
            r = writeRow(sheet, r, new String[]{"豁免", nvl(h.getSave())}, valueStyle);
            if (h.getEquip() != null && !h.getEquip().isEmpty()) {
                r = writeRow(sheet, r, new String[]{"装备", h.getEquip()}, valueStyle);
            }

            // ===== Other =====
            if (h.getOther() != null && !h.getOther().isEmpty()) {
                r = writeRow(sheet, r, new String[]{"其它", h.getOther()}, valueStyle);
            }

            // ===== Special (Style, Fight, Superspell, Pray, Feat) =====
            r++;
            r = writeRow(sheet, r, new String[]{"特殊"}, sectionStyle);

            // Fight style - label + list
            String styleVal = h.getStyle() != null && !h.getStyle().isEmpty() ? h.getStyle() : "";
            r = writeRow(sheet, r, new String[]{"战斗风格", styleVal}, valueStyle);

            // Fight + Superspell merged table: name / list / points
            String fightVal = h.getFight() != null && !h.getFight().isEmpty() ? h.getFight() : "";
            String superVal = h.getSuperspell() != null && !h.getSuperspell().isEmpty() ? h.getSuperspell() : "";
            r = writeRow(sheet, r, new String[]{"名称", "列表", "点数"}, labelStyle);
            {
                Row sr = sheet.createRow(r++);
                createCell(sr, 0, "战技", valueStyle); createCell(sr, 1, fightVal, valueStyle); createCell(sr, 2, "", blankStyle);
            }
            {
                Row tr = sheet.createRow(r++);
                createCell(tr, 0, "超魔法", valueStyle); createCell(tr, 1, superVal, valueStyle); createCell(tr, 2, "", blankStyle);
            }

            // Pray (魔能祈唤) - each row: name / points1 / points2
            List<String> prayItems = parseList(h.getPray());
            r = writeRow(sheet, r, new String[]{"魔能祈唤", "点数1", "点数2"}, labelStyle);
            if (prayItems.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    Row pr = sheet.createRow(r++);
                    createCell(pr, 0, "", blankStyle); createCell(pr, 1, "", blankStyle); createCell(pr, 2, "", blankStyle);
                }
            } else {
                for (String p : prayItems) {
                    Row pr = sheet.createRow(r++);
                    createCell(pr, 0, p.trim(), valueStyle); createCell(pr, 1, "", blankStyle); createCell(pr, 2, "", blankStyle);
                }
            }

            // Feats (专长) - each row: name / points1 / points2
            List<String> featItems = parseList(h.getFeat());
            r = writeRow(sheet, r, new String[]{"专长", "点数1", "点数2"}, labelStyle);
            if (featItems.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    Row fr = sheet.createRow(r++);
                    createCell(fr, 0, "", blankStyle); createCell(fr, 1, "", blankStyle); createCell(fr, 2, "", blankStyle);
                }
            } else {
                for (String f : featItems) {
                    Row fr = sheet.createRow(r++);
                    createCell(fr, 0, f.trim(), valueStyle); createCell(fr, 1, "", blankStyle); createCell(fr, 2, "", blankStyle);
                }
            }

            // ===== Spells =====
            List<String> s0 = parseList(h.getSpell0()).stream().filter(s -> !s.endsWith("-法表")).collect(Collectors.toList());
            List<String> s1 = parseList(h.getSpell1()).stream().filter(s -> !s.endsWith("-法表")).collect(Collectors.toList());
            List<String> s2 = parseList(h.getSpell2()).stream().filter(s -> !s.endsWith("-法表")).collect(Collectors.toList());
            r = writeRow(sheet, r, new String[]{"法术"}, sectionStyle);
            r = writeRow(sheet, r, new String[]{"戏法", s0.isEmpty() ? "" : String.join(", ", s0)}, valueStyle);
            r = writeRow(sheet, r, new String[]{"1环", s1.isEmpty() ? "" : String.join(", ", s1)}, valueStyle);
            r = writeRow(sheet, r, new String[]{"2环", s2.isEmpty() ? "" : String.join(", ", s2)}, valueStyle);
            for (int lvl = 3; lvl <= 9; lvl++) {
                r = writeRow(sheet, r, new String[]{lvl + "环", ""}, blankStyle);
            }

            // ===== Background =====
            r++;
            r = writeRow(sheet, r, new String[]{"背景"}, sectionStyle);
            r = writeRow(sheet, r, new String[]{"年龄", nvl(h.getAge())}, valueStyle);
            r = writeRow(sheet, r, new String[]{"生活开支", nvl(h.getLive())}, valueStyle);

            // ===== Money =====
            r = writeRow(sheet, r, new String[]{"资金", (h.getMoney() != null ? h.getMoney() : 0) + " GP"}, valueStyle);

            // ===== Item list =====
            r++;
            r = writeRow(sheet, r, new String[]{"物品列表"}, sectionStyle);
            for (int i = 0; i < 8; i++) {
                Row itemr = sheet.createRow(r++);
                for (int j = 0; j < 4; j++) createCell(itemr, j, "", blankStyle);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("导出失败: " + e.getMessage(), e);
        }
    }

    private int writeSection(Sheet sheet, int r, String title, List<String> items,
                              CellStyle lbl, CellStyle val, CellStyle blank) {
        Row hdr = sheet.createRow(r++);
        createCell(hdr, 0, title, lbl);
        createCell(hdr, 1, "数值", lbl);
        for (String item : items) {
            Row row = sheet.createRow(r++);
            createCell(row, 0, item, val);
            createCell(row, 1, "", blank);
        }
        return r;
    }

    private int attrMod(String attrName, int[] attrs) {
        String[] attrNames = {"力量","敏捷","体质","智力","感知","魅力"};
        for (int i = 0; i < attrNames.length; i++) {
            if (attrNames[i].equals(attrName)) return Math.floorDiv(attrs[i], 2) - 5;
        }
        return 0;
    }

    private int writeRow(Sheet sheet, int r, String[] values, CellStyle style) {
        Row row = sheet.createRow(r++);
        for (int i = 0; i < values.length; i++) {
            createCell(row, i, values[i], style);
        }
        return r;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }

    private String nvl(String s) { return s == null ? "" : s; }
    private String nvl(Integer i) { return i == null ? "" : String.valueOf(i); }

    private int[] parseAttrs(String attr) {
        if (attr == null) return new int[]{0, 0, 0, 0, 0, 0};
        String[] p = attr.split(",");
        int[] a = new int[6];
        for (int i = 0; i < 6 && i < p.length; i++) {
            try { a[i] = Integer.parseInt(p[i].trim()); } catch (NumberFormatException e) { a[i] = 0; }
        }
        return a;
    }

    private String[] parseCareerList(String career) {
        if (career == null || career.isEmpty()) return new String[0];
        return career.split(",");
    }

    private List<String> parseList(String s) {
        if (s == null || s.isEmpty()) return Collections.emptyList();
        return Arrays.asList(s.split(","));
    }

    // ===== Cell Styles =====

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 14);
        s.setFont(f); s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle createSectionStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 12);
        s.setFont(f);
        return s;
    }

    private CellStyle createLabelStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont(); f.setBold(true); f.setFontHeightInPoints((short) 11);
        s.setFont(f); s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle createValueStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setAlignment(HorizontalAlignment.LEFT);
        Font f = wb.createFont(); f.setFontHeightInPoints((short) 11);
        s.setFont(f);
        return s;
    }

    private CellStyle createBlankStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }
}
