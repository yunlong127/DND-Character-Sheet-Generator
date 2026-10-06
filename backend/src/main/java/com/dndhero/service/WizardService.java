package com.dndhero.service;

import com.dndhero.model.dto.*;
import com.dndhero.model.entity.*;
import com.dndhero.repository.*;
import com.dndhero.util.DiceParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WizardService {

    private static final Logger log = LoggerFactory.getLogger(WizardService.class);

    private final ChooseRepository chooseRepo;
    private final RaceRepository raceRepo;
    private final SubraceRepository subraceRepo;
    private final CareerRepository careerRepo;
    private final SubcareerRepository subcareerRepo;
    private final BackgroundRepository bgRepo;
    private final NameTableRepository nameRepo;
    private final HeroRepository heroRepo;
    private final AttributeRepository attrRepo;
    private final MoneyRepository moneyRepo;
    private final SkillRepository skillRepo;
    private final EquipRepository equipRepo;
    private final ToolRepository toolRepo;
    private final LanguageRepository langRepo;
    private final FeatRepository featRepo;
    private final SaveRepository saveRepo;
    private final SpellRepository spellRepo;
    private final StyleRepository styleRepo;
    private final SuperspellRepository superspellRepo;
    private final FightRepository fightRepo;
    private final InformationRepository infoRepo;
    private final PrayRepository prayRepo;

    private Map<Integer, Choose> chooseMap;
    private Map<String, Attribute> attrMap;
    private List<Race> allRaces;
    private List<Subrace> allSubraces;
    private List<Career> allCareers;
    private List<Subcareer> allSubcareers;
    private Map<String, Background> bgMap;
    private List<NameTable> allNames;
    private List<Feat> allFeats;
    private List<Pray> allPrays;
    private Map<String, Money> moneyMap;
    private Map<String, String> classifyMap = new HashMap<>();
    private Map<String, Style> styleMap = new HashMap<>();
    private Map<String, Pray> prayMap = new HashMap<>();
    private Map<String, Feat> featMap = new HashMap<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, String> skillBeforeMap = new HashMap<>();
    private Map<String, String> toolBeforeMap = new HashMap<>();

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public WizardService(ChooseRepository cr, RaceRepository rr, SubraceRepository sr, CareerRepository crr,
            SubcareerRepository scr, BackgroundRepository bg, NameTableRepository nr, HeroRepository hr,
            AttributeRepository ar, FeatRepository fr, PrayRepository pr, MoneyRepository mr,
            SkillRepository skr, EquipRepository er, ToolRepository tr, LanguageRepository lr,
            SaveRepository svr, SpellRepository spr, StyleRepository str, SuperspellRepository ssr,
            FightRepository fir, InformationRepository ir) {
        chooseRepo = cr; raceRepo = rr; subraceRepo = sr; careerRepo = crr; subcareerRepo = scr;
        bgRepo = bg; nameRepo = nr; heroRepo = hr; attrRepo = ar; featRepo = fr; prayRepo = pr;
        moneyRepo = mr; skillRepo = skr; equipRepo = er; toolRepo = tr; langRepo = lr;
        saveRepo = svr; spellRepo = spr; styleRepo = str; superspellRepo = ssr;
        fightRepo = fir; infoRepo = ir;
        init();
    }

    private void init() {
        chooseMap = chooseRepo.findAll().stream().collect(Collectors.toMap(Choose::getId, c -> c));
        attrMap = attrRepo.findAll().stream().collect(Collectors.toMap(Attribute::getName, a -> a));
        allRaces = raceRepo.findAll();
        allSubraces = subraceRepo.findAll();
        allCareers = careerRepo.findAll();
        allSubcareers = subcareerRepo.findAll();
        bgMap = bgRepo.findAll().stream().collect(Collectors.toMap(Background::getName, b -> b));
        allNames = nameRepo.findAll();
        allFeats = featRepo.findAll();
        featMap = allFeats.stream().collect(Collectors.toMap(Feat::getName, f -> f));
        allPrays = prayRepo.findAll();
        moneyMap = moneyRepo.findAll().stream().collect(Collectors.toMap(Money::getName, m -> m));
        skillRepo.findAll().forEach(e -> { classifyMap.put(e.getName(), "skill"); if (e.getBefore() != null && !e.getBefore().isEmpty()) skillBeforeMap.put(e.getName(), e.getBefore()); });
         equipRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "equip"));
        toolRepo.findAll().forEach(e -> { classifyMap.put(e.getName(), "tool"); if (e.getBefore() != null && !e.getBefore().isEmpty()) toolBeforeMap.put(e.getName(), e.getBefore()); });
        langRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "language"));
        featRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "feat"));
        saveRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "save"));
        allPrays.forEach(e -> { classifyMap.put(e.getName(), "pray"); prayMap.put(e.getName(), e); });
        styleRepo.findAll().forEach(e -> { classifyMap.put(e.getName(), "style"); styleMap.put(e.getName(), e); });
        superspellRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "superspell"));
        fightRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "fight"));
        infoRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "other"));
        spellRepo.findAll().forEach(s -> { classifyMap.put(s.getName(), "spell"); spellLevels.put(s.getName(), s.getLevel()); });
        Set.of("通用语").forEach(s -> classifyMap.putIfAbsent(s, "language"));
        Set.of("轻甲","中甲","重甲","盾牌","简易","军用","火器","手弩","长剑","刺剑","短剑")
            .forEach(s -> classifyMap.putIfAbsent(s, "equip"));
    }

    public WizardStepResponse startWizard(String formula, String background, int rollCount, String mode) {
        Session s = new Session();
        s.id = UUID.randomUUID().toString().substring(0, 8);
        s.formula = formula != null ? formula : "5d6d1d1";
        s.bgName = background != null ? background : "背景-强化";
        s.mode = "input".equals(mode) ? mode : "roll";
        int n = Math.max(1, Math.min(rollCount, 10));
        s.log.append("========== 角色创建向导 ==========\n");
        s.log.append("sessionId=").append(s.id).append(" mode=").append(s.mode).append("\n");
        s.log.append("公式=").append(s.formula).append(" 背景=").append(s.bgName).append(" 重骰数=").append(n).append("\n");
        s.log.append("性别=随机 → ").append(s.sex).append("\n");
        if ("input".equals(s.mode)) {
            s.step = STEP_ATTRS_INPUT;
            sessions.put(s.id, s);
            return drainOrAdvance(s);
        }
        s.log.append("--- 属性池 ---\n");
        for (int r = 0; r < n; r++) {
            int[] arr = new int[6];
            for (int i = 0; i < 6; i++) arr[i] = DiceParser.roll(s.formula);
            s.attrPool.add(arr);
            s.log.append("  组").append(r+1).append(": ").append(attrStr(arr)).append(" 总和").append(sum(arr)).append("\n");
        }
        s.attrs = s.attrPool.get(0);
        sessions.put(s.id, s);
        return drainOrAdvance(s);
    }

    /**
     * User picks from candidates. Returns the next step.
     */
    public WizardStepResponse userChoice(String sessionId, List<String> picked) {
        Session s = sessions.get(sessionId);
        if (s == null) { WizardStepResponse r = new WizardStepResponse(); r.setStepType("error"); r.setDescription("会话已过期"); return r; }
        if (s.finalized) return s.lastFinalResp;
        if (picked == null) picked = Collections.emptyList();

        s.log.append("--- 步骤: ").append(stepName(s.step)).append(" ---\n");
        s.log.append("  选择: ").append(picked).append("\n");
        int beforeSize = s.content.size();

        String stepType = s.pendingStep != null ? s.pendingStep.type : "";

        for (String p : picked) {
            p = s.resolvePick(p.trim());

            switch (stepType) {
                case "pick_attrs":
                    try { int idx = Integer.parseInt(p); s.attrs = s.attrPool.get(idx);
                        s.log.append("  → 属性组").append(idx+1).append(": ").append(attrStr(s.attrs)).append(" 总和").append(sum(s.attrs)).append("\n");
                    } catch (Exception ignored) {}
                    s.step = STEP_RACE;
                    continue;
                case "pick_attrs_input":
                    s.attrsInput = p;
                    try {
                        String[] vs = p.split(",");
                        for (int i = 0; i < 6 && i < vs.length; i++) s.attrs[i] = Integer.parseInt(vs[i].trim());
                    } catch (Exception ignored) {}
                    s.log.append("  → 输入属性: ").append(attrStr(s.attrs)).append(" 总和").append(sum(s.attrs)).append("\n");
                    s.attrSetDone = true;
                    s.step = STEP_RACE;
                    continue;
                case "pick_race":
                    s.race = findRace(p); if (s.race == null) { s.error = "种族不存在"; saveLog(s); return err(s.error); }
                    s.add(p); s.addAll(s.race.getAbility()); s.addAll(s.race.getSkill());
                    queueChooses(s.race.getChoose(), s); // queue race chooses
                    s.log.append("  → 种族: ").append(p).append("\n");
                    s.step = STEP_SUBRACE;
                    continue;
                case "pick_subrace":
                    s.subrace = findSubrace(p); if (s.subrace == null) { s.error = "亚种不存在"; saveLog(s); return err(s.error); }
                    s.add(p); applySubAttrs(s); s.addAll(s.subrace.getAbility()); s.addAll(s.subrace.getSkill());
                    if (s.subrace.getCanSpell() != null && s.subrace.getCanSpell()) s.canSpell = true;
                    queueChooses(s.subrace.getChoose(), s); // queue subrace chooses
                    s.subDone = true;
                    s.step = STEP_FINAL_PREP;
                    s.log.append("  → 亚种: ").append(p).append("\n");
                    continue;
                case "pick_career":
                    s.career = findCareer(p); if (s.career == null) { s.error = "职业不存在"; saveLog(s); return err(s.error); }
                    s.add(p); s.addAll(s.career.getAbility()); s.addAll(s.career.getSkill());
                    if (s.career.getCanSpell() != null && s.career.getCanSpell()) s.canSpell = true;
                    queueChooses(s.career.getChoose(), s); // queue career chooses
                    s.step = STEP_SUBCAREER;
                    s.log.append("  → 职业: ").append(p).append("\n");
                    continue;
                case "pick_subcareer":
                    s.subcareer = findSubcareer(p); if (s.subcareer == null) { s.error = "子职不存在"; saveLog(s); return err(s.error); }
                    s.add(p); s.addAll(s.subcareer.getAbility()); s.addAll(s.subcareer.getSkill());
                    queueChooses(s.subcareer.getChoose(), s); // queue subcareer chooses
                    s.scDone = true;
                    s.step = "input".equals(s.mode) ? STEP_MONEY : STEP_FINAL;
                    s.log.append("  → 子职: ").append(p).append("\n");
                    continue;
                case "pick_sex":
                    s.sex = p; s.sexPicked = true;
                    s.step = STEP_NAME_RACE;
                    s.log.append("  → 性别: ").append(p).append("\n");
                    continue;
                case "pick_camp":
                    s.campPicked = true;
                    s.campIdx = campIdxOf(p);
                    s.step = STEP_BG;
                    s.log.append("  → 阵营: ").append(p).append(" idx=").append(s.campIdx).append("\n");
                    addAttrTextOrBonus(s, p);
                    continue;
                case "pick_name_race":
                    s.nameRace = p; s.nameRacePicked = true;
                    // Check if selected item is a numeric choose ID with sub-options
                    List<String> subOpts = subNameOptions(p);
                    if (subOpts != null) {
                        s.pendingStep = new WizStep("pick_name_race_sub", "选择姓名分支(" + p + ")", subOpts, 1, null);
                        s.log.append("  → 姓名种族(子): ").append(p).append(" → 进入子选择\n");
                        return toResponse(s);
                    } else {
                        resolveNameTableByRace(s, p);
                        s.step = STEP_NAME_FIRST;
                        s.log.append("  → 姓名种族: ").append(p).append("\n");
                    }
                    continue;
                case "pick_name_race_sub":
                    s.log.append("  → 姓名分支: ").append(p).append("\n");
                    // Recursively check for sub-sub options
                    List<String> subSubOpts = subNameOptions(p);
                    if (subSubOpts != null) {
                        s.pendingStep = new WizStep("pick_name_race_sub", "选择姓名分支(" + p + ")", subSubOpts, 1, null);
                        return toResponse(s);
                    } else {
                        resolveNameTableByRace(s, p);
                        s.step = STEP_NAME_FIRST;
                    }
                    continue;
                case "pick_name_first":
                    s.nameFirst = p; s.nameFirstPicked = true;
                    s.step = STEP_NAME_LAST;
                    s.log.append("  → 名字: ").append(p).append("\n");
                    continue;
                case "pick_name_last":
                    s.nameLast = p; s.nameLastPicked = true;
                    s.step = STEP_NAME_ALIAS;
                    s.log.append("  → 姓氏: ").append(p).append("\n");
                    continue;
                case "pick_name_alias":
                    s.nameAlias = p; s.nameAliasPicked = true;
                    s.step = STEP_AGE;
                    s.log.append("  → 别名: ").append(p).append("\n");
                    continue;
                case "pick_age":
                    try { String[] ps = p.split(","); s.ageYears = Integer.parseInt(ps[0].trim()); s.ageMonths = ps.length > 1 ? Integer.parseInt(ps[1].trim()) : 0; } catch (Exception ignored) {}
                    s.agePicked = true;
                    s.step = STEP_CAMP;
                    s.log.append("  → 年龄: ").append(s.ageYears).append(",").append(s.ageMonths).append("\n");
                    continue;
                case "pick_money":
                    try { s.moneyValue = Integer.parseInt(p.trim()); } catch (Exception ignored) {}
                    s.moneyPicked = true;
                    s.step = STEP_FINAL;
                    s.log.append("  → 金钱: ").append(p).append(" GP\n");
                    continue;
                case "pick_choose":
                    // Handle numeric choose references
                    try {
                        int refId = Integer.parseInt(p);
                        Choose refCh = chooseMap.get(refId);
                        if (refCh != null) {
                            if (refCh.getChoose() != null && !refCh.getChoose().isEmpty()) {
                                s.log.append("  → choose(").append(refId).append(") → 子项展开\n");
                                for (String sub : refCh.getChoose().split(",")) {
                                    try { s.chooseQueue.addFirst(Integer.parseInt(sub.trim())); } catch (Exception ignored) {}
                                }
                            } else if (refCh.getList() != null && !refCh.getList().isEmpty()) {
                                s.chooseQueue.addFirst(refId);
                            } else {
                                addAttrTextOrBonus(s, p);
                            }
                        } else { addAttrTextOrBonus(s, p); }
                    } catch (NumberFormatException e) {
                        addAttrTextOrBonus(s, p);
                    }
                    s.log.append("  → 已选择: ").append(p).append("\n");
                    continue;
            }
            // Fallback: just add to content
            addAttrTextOrBonus(s, p);
            s.log.append("  → 添加: ").append(p).append("\n");
        }
        if (s.content.size() != beforeSize) {
            s.log.append("  → 内容变化: ").append(beforeSize).append(" → ").append(s.content.size()).append("项\n");
            s.log.append("  → 当前内容: ").append(new ArrayList<>(s.content)).append("\n");
        }
        s.pendingStep = null;
        return drainOrAdvance(s);
    }

    private void applySubAttrs(Session s) {
        if (s.subrace == null || s.subrace.getAttribute() == null || s.subrace.getAttribute().length() < 6) return;
        for (int i = 0; i < 6; i++) { char c = s.subrace.getAttribute().charAt(i); if (c >= '0' && c <= '9') s.attrs[i] += (c - '0'); }
    }

    /** Add item to content, but if it's an attribute name (力量/敏捷/...), apply bonus to attrs instead */
    private void addAttrTextOrBonus(Session s, String item) {
        if (attrMap.containsKey(item)) {
            int idx = attrMap.get(item).getId();
            s.attrs[idx]++;
            s.log.append("  → 属性: ").append(item).append("+1 → ").append(Arrays.toString(s.attrs)).append("\n");
        } else {
            s.add(item);
            // Check feat: ability bonus, canSpell, cascading chooses
            Feat ft = featMap.get(item);
            if (ft != null) {
                if (ft.getAbility() != null) {
                    for (String a : ft.getAbility().split(",")) {
                        String aa = a.trim();
                        if (attrMap.containsKey(aa)) {
                            int idx = attrMap.get(aa).getId();
                            s.attrs[idx]++;
                            s.log.append("  → 属性(").append(item).append("): ").append(aa).append("+1 → ").append(Arrays.toString(s.attrs)).append("\n");
                        } else if (!aa.isEmpty()) {
                            s.add(aa);
                            s.log.append("  → 专长(").append(item).append("): +").append(aa).append("\n");
                        }
                    }
                }
                if (ft.getCanSpell() != null && ft.getCanSpell()) s.canSpell = true;
                if (ft.getChoose() != null && !ft.getChoose().isEmpty()) {
                    s.log.append("  → 专长 ").append(item).append(" 级联 choose=").append(ft.getChoose()).append("\n");
                    enqueueStarsLast(ft.getChoose(), s);
                }
            }
            // Check if this is a style with cascading chooses
            Style st = styleMap.get(item);
            if (st != null && st.getChoose() != null && !st.getChoose().isEmpty()) {
                s.log.append("  → 风格 ").append(item).append(" 级联 choose=").append(st.getChoose()).append("\n");
                enqueueStarsLast(st.getChoose(), s);
            }
            // Check if this is a pray with cascading chooses or ability
            Pray p = prayMap.get(item);
            if (p != null) {
                if (p.getAbility() != null) {
                    for (String a : p.getAbility().split(",")) {
                        String aa = a.trim();
                        if (attrMap.containsKey(aa)) {
                            int idx = attrMap.get(aa).getId();
                            s.attrs[idx]++;
                            s.log.append("  → 属性(").append(item).append("): ").append(aa).append("+1 → ").append(Arrays.toString(s.attrs)).append("\n");
                        } else if (!aa.isEmpty()) {
                            s.add(aa);
                            s.log.append("  → 祈祷(").append(item).append("): +").append(aa).append("\n");
                        }
                    }
                }
                if (p.getChoose() != null && !p.getChoose().isEmpty()) {
                    s.log.append("  → 祈祷 ").append(item).append(" 级联 choose=").append(p.getChoose()).append("\n");
                    enqueueStarsLast(p.getChoose(), s);
                }
                if (p.getCanSpell() != null && p.getCanSpell()) s.canSpell = true;
            }
        }
    }

    private void enqueueStarsLast(String ids, Session s) {
        String[] subs = ids.split(",");
        for (int i = subs.length - 1; i >= 0; i--) {
            try { s.chooseQueue.addFirst(Integer.parseInt(subs[i].trim())); } catch (Exception ignored) {}
        }
    }

    private String stepName(int step) {
        switch (step) {
            case STEP_ATTRS: return "属性选择";
            case STEP_ATTRS_INPUT: return "输入属性";
            case STEP_RACE: return "种族选择";
            case STEP_SUBRACE: return "亚种选择";
            case STEP_NAME_RACE: return "姓名种族";
            case STEP_NAME_FIRST: return "名字";
            case STEP_NAME_LAST: return "姓氏";
            case STEP_NAME_ALIAS: return "别名";
            case STEP_AGE: return "年龄";
            case STEP_CAMP: return "阵营选择";
            case STEP_BG: return "背景选项";
            case STEP_CAREER: return "职业选择";
            case STEP_SUBCAREER: return "子职选择";
            case STEP_FINAL_PREP: return "性别选择";
            case STEP_MONEY: return "金钱输入";
            case STEP_FINAL: return "最终生成";
            default: return "选择";
        }
    }

    private WizardStepResponse drainOrAdvance(Session s) {
        s.log.append("  [drainOrAdvance step=").append(s.step).append(" queue=").append(s.chooseQueue.size()).append("]\n");
        // Drain ALL queued chooses first
        while (true) {
            WizStep q = popChooseRecursive(s);
            if (q != null) {
                s.pendingStep = q;
                return toResponse(s);
            }
            // Queue empty → advance one phase
            if (advancePhase(s)) {
                return toResponse(s);
            }
            if (s.error != null) { saveLog(s); return err(s.error); }
        }
    }

    /** Returns true if a user step was generated (halt), false to continue looping */
    private boolean advancePhase(Session s) {
        s.log.append("\n[phase=").append(s.step).append(" ").append(stepName(s.step)).append("]\n");
        switch (s.step) {
            case STEP_ATTRS:
                List<List<Integer>> opts = new ArrayList<>();
                List<String> labels = new ArrayList<>();
                for (int i = 0; i < s.attrPool.size(); i++) {
                    int[] a = s.attrPool.get(i);
                    opts.add(ints(a)); labels.add("组" + (i + 1) + ": " + attrStr(a) + " 总和" + sum(a));
                }
                s.pendingStep = new WizStep("pick_attrs", "选择属性组", labels, 1, opts);
                return true;
            case STEP_ATTRS_INPUT:
                if (s.attrsInput == null) {
                    int min = DiceParser.minVal(s.formula);
                    int max = DiceParser.maxVal(s.formula);
                    s.pendingStep = new WizStep("pick_attrs_input", "输入6项属性 (" + min + "~" + max + ")", Collections.emptyList(), 0, null);
                    return true;
                }
                if (!s.attrSetDone) { s.attrSetDone = true; s.step = STEP_RACE; return false; }
                s.step = STEP_RACE;
                return false;
            case STEP_RACE:
                if (s.race == null) {
                    s.pendingStep = new WizStep("pick_race", "选择种族", raceNames(), 1, null);
                    return true;
                }
                s.step = STEP_SUBRACE;
                return false;
            case STEP_SUBRACE:
                if (!s.subDone) {
                    List<String> srNames = subraceNames(s);
                    if (srNames != null && srNames.size() > 1) {
                        s.pendingStep = new WizStep("pick_subrace", "选择亚种", srNames, 1, null);
                        return true;
                    }
                    if (srNames != null && srNames.size() == 1) {
                        s.subrace = findSubrace(srNames.get(0)); s.add(srNames.get(0));
                        applySubAttrs(s); s.addAll(s.subrace.getAbility()); s.addAll(s.subrace.getSkill());
                        if (s.subrace.getCanSpell() != null && s.subrace.getCanSpell()) s.canSpell = true;
                        queueChooses(s.subrace.getChoose(), s);
                    }
                    s.subDone = true;
                }
                if (drainChoose(s)) return true;
                s.step = STEP_FINAL_PREP;
                return false;
            case STEP_FINAL_PREP:
                if (!s.sexPicked) {
                    s.pendingStep = new WizStep("pick_sex", "选择性别", Arrays.asList("男", "女"), 1, null);
                    return true;
                }
                s.step = STEP_NAME_RACE;
                return false;
            case STEP_NAME_RACE: {
                if (!s.nameRacePicked) {
                    List<String> nameOpts = raceNameOptions(s);
                    if (nameOpts.isEmpty()) {
                        if (!allNames.isEmpty()) resolveNameTableByRace(s, allNames.get(new Random().nextInt(allNames.size())).getName());
                        s.nameRacePicked = true;
                        s.step = STEP_NAME_FIRST;
                        return false;
                    }
                    s.pendingStep = new WizStep("pick_name_race", "选择姓名种族", nameOpts, 1, null);
                    return true;
                }
                s.step = STEP_NAME_FIRST;
                return false;
            }
            case STEP_NAME_FIRST: {
                if (!s.nameFirstPicked) {
                    List<String> firsts = firstNameOptions(s);
                    s.pendingStep = new WizStep("pick_name_first", "名字", firsts, 1, null);
                    return true;
                }
                s.step = STEP_NAME_LAST;
                return false;
            }
            case STEP_NAME_LAST: {
                if (!s.nameLastPicked) {
                    List<String> lasts = lastNameOptions(s);
                    if (lasts.isEmpty()) { s.nameLastPicked = true; s.step = STEP_NAME_ALIAS; return false; }
                    s.pendingStep = new WizStep("pick_name_last", "姓氏", lasts, 1, null);
                    return true;
                }
                s.step = STEP_NAME_ALIAS;
                return false;
            }
            case STEP_NAME_ALIAS: {
                if (!s.nameAliasPicked) {
                    List<String> aliases = aliasOptions(s);
                    if (aliases.isEmpty()) { s.nameAliasPicked = true; s.step = STEP_AGE; return false; }
                    s.pendingStep = new WizStep("pick_name_alias", "别名", aliases, 1, null);
                    return true;
                }
                s.step = STEP_AGE;
                return false;
            }
            case STEP_AGE: {
                if (!s.agePicked && s.subrace != null) {
                    int[] range = ageRange(s.subrace);
                    if (range[0] > 0 && range[1] > 0) {
                        s.pendingStep = new WizStep("pick_age", "年龄 (" + range[0] + "~" + range[1] + "岁) 输入:年,月",
                                Collections.emptyList(), 0, null);
                        return true;
                    }
                }
                s.agePicked = true;
                s.step = STEP_CAMP;
                return false;
            }
            case STEP_CAMP: {
                if (!s.campPicked) {
                    s.campMask = "111111111"; // manual: all 9 alignments selectable
                    List<String> campOpts = campOptions(s.campMask);
                    s.pendingStep = new WizStep("pick_camp", "选择阵营", campOpts, 1, null);
                    return true;
                }
                s.step = STEP_BG;
                return false;
            }
            case STEP_BG:
                if (!s.bgApplied) { applyBackground(s); s.bgApplied = true; }
                if (drainChoose(s)) return true;
                s.step = STEP_CAREER;
                return false;
            case STEP_CAREER:
                if (s.career == null) {
                    List<String> cc = allCareers.stream().filter(c -> checkCareer(c, s) && campCompatible(c.getCamp(), s)).map(Career::getName).collect(Collectors.toList());
                    if (cc.isEmpty()) { s.error = "无可就职职业"; return false; }
                    s.pendingStep = new WizStep("pick_career", "选择职业", cc, 1, null);
                    return true;
                }
                s.step = STEP_SUBCAREER;
                return false;
            case STEP_SUBCAREER:
                if (!s.scDone) {
                    List<String> scNames = subcareerNames(s);
                    if (scNames != null) scNames = scNames.stream().filter(n -> {
                        Subcareer sc = findSubcareer(n);
                        return sc == null || campCompatible(sc.getCamp(), s);
                    }).collect(Collectors.toList());
                    if (scNames != null && scNames.size() > 1) {
                        s.pendingStep = new WizStep("pick_subcareer", "选择子职", scNames, 1, null);
                        return true;
                    }
                    if (scNames != null && scNames.size() == 1) {
                        s.subcareer = findSubcareer(scNames.get(0)); s.add(scNames.get(0));
                        s.addAll(s.subcareer.getAbility()); s.addAll(s.subcareer.getSkill());
                        queueChooses(s.subcareer.getChoose(), s);
                    }
                    s.scDone = true;
                }
                s.step = "input".equals(s.mode) ? STEP_MONEY : STEP_FINAL;
                return false;
            case STEP_MONEY: {
                if (!s.moneyPicked && s.career != null && s.career.getMoney() != null) {
                    String mf = s.career.getMoney();
                    String[] mp = mf.split(",");
                    if (mp.length == 3) {
                        int a = Integer.parseInt(mp[0].trim()), b = Integer.parseInt(mp[1].trim()), c = Integer.parseInt(mp[2].trim());
                        int min = a * c, max = a * b * c;
                        s.pendingStep = new WizStep("pick_money", "金钱 " + min + "~" + max + " GP",
                                Collections.emptyList(), 0, null);
                        return true;
                    }
                }
                s.moneyPicked = true;
                s.step = STEP_FINAL;
                return false;
            }
            case STEP_FINAL:
                if (s.finalized) return true;
                s.lastFinalResp = buildFinal(s);
                s.finalized = true;
                return true;
            default:
                s.error = "状态异常"; return false;
        }
    }

    private static final int STEP_ATTRS = 0, STEP_ATTRS_INPUT = 1, STEP_RACE = 2, STEP_SUBRACE = 3,
        STEP_FINAL_PREP = 4, // sex
        STEP_NAME_RACE = 5, STEP_NAME_FIRST = 6, STEP_NAME_LAST = 7, STEP_NAME_ALIAS = 8,
        STEP_AGE = 9, STEP_CAMP = 10, STEP_BG = 11, STEP_CAREER = 12, STEP_SUBCAREER = 13,
        STEP_MONEY = 14, STEP_FINAL = 15;

    private static final String[] AL = {"守序善良","中立善良","混乱善良","守序中立","绝对中立","混乱中立","守序邪恶","中立邪恶","混乱邪恶"};

    private void calcCampMask(Session s) {
        String r = "111111111";
        if (s.subrace != null && s.subrace.getCamp() != null) r = campAnd(r, s.subrace.getCamp());
        s.campMask = r;
        s.log.append("  → 阵营候选(亚种=").append(s.subrace != null ? s.subrace.getName() : "?").append("): ").append(campOptions(r)).append("\n");
    }

    private List<String> campOptions(String mask) {
        List<String> opts = new ArrayList<>();
        for (int i = 0; i < mask.length(); i++) if (mask.charAt(i) == '1') opts.add(AL[i]);
        if (opts.isEmpty()) opts.add("绝对中立");
        return opts;
    }

    private int[] ageRange(Subrace sr) {
        if (sr == null || sr.getAge() == null) return new int[]{0,0};
        String[] p = sr.getAge().split(",");
        if (p.length < 2) return new int[]{0,0};
        try {
            int a = Integer.parseInt(p[0].trim()), b = Integer.parseInt(p[1].trim());
            return new int[]{(int)Math.ceil(0.75*a), (int)Math.floor(0.5*b)};
        } catch (Exception ignored) { return new int[]{0,0}; }
    }

    /** Pop and return a step, or false if queue emptied */
    private boolean drainChoose(Session s) {
        WizStep ws = popChooseRecursive(s);
        if (ws != null) { s.pendingStep = ws; return true; }
        return false;
    }

    /** Like popChoose but resolves nested 'choose' references to flat items */
    private WizStep popChooseRecursive(Session s) {
        while (!s.chooseQueue.isEmpty()) {
            int cid = s.chooseQueue.pollFirst();
            Choose ch = chooseMap.get(cid);
            if (ch == null) continue;
            // If this has sub-chooses, expand them into the queue
            if (ch.getChoose() != null && !ch.getChoose().isEmpty()) {
                s.log.append("  → 展开 choose(").append(cid).append(") → ").append(ch.getChoose()).append("\n");
                String[] subs = ch.getChoose().split(",");
                for (int i = subs.length - 1; i >= 0; i--) {
                    try { s.chooseQueue.addFirst(Integer.parseInt(subs[i].trim())); } catch (Exception ignored) {}
                }
                continue;
            }
            // Leaf choose: filter its list and show
            if (ch.getList() == null || ch.getList().isEmpty()) continue;
            List<String> cands = filter(ch.getList().split(","), ch.getExtend(), s);
            if (cands.isEmpty()) { s.log.append("  → choose(").append(cid).append(") 过滤为空，跳过\n"); continue; }
            // cnt < 0 → auto-add all candidates, no user step
            if (ch.getCnt() != null && ch.getCnt() < 0) {
                for (String c : cands) addAttrTextOrBonus(s, c);
                s.log.append("  → choose(").append(cid).append(") cnt=-1 自动添加全部 ").append(cands.size()).append("项\n");
                continue;
            }
            // Build display labels (handle duplicates by appending suffix)
            s.labelToRaw.clear();
            List<String> display = new ArrayList<>();
            s.dupeIndex.clear();
            for (String c : cands) {
                String label = labelForChooseItem(c);
                if (s.dupeIndex.containsKey(label)) {
                    int idx = s.dupeIndex.get(label) + 1;
                    s.dupeIndex.put(label, idx);
                    String uniqueLabel = label + " (" + idx + ")";
                    display.add(uniqueLabel);
                    s.labelToRaw.put(uniqueLabel, c);
                } else {
                    s.dupeIndex.put(label, 1);
                    display.add(label);
                    if (!label.equals(c)) s.labelToRaw.put(label, c);
                }
            }
            String title = ch.getExtend() != null && !ch.getExtend().isEmpty() ? ch.getExtend() : "choose(" + cid + ")";
            int cnt = ch.getCnt() != null && ch.getCnt() > 0 ? Math.min(ch.getCnt(), display.size()) : display.size();
            s.log.append("  → 展示 choose(").append(cid).append(") \"").append(title).append("\" cnt=").append(cnt).append(" 候选=").append(display.size()).append("\n");
            return new WizStep("pick_choose", title, display, cnt, null);
        }
        return null;
    }

    // ==================== Apply helpers (inlined into userChoice) ====================

    private void applyBackground(Session s) {
        Background bg = bgMap.get(s.bgName);
        if (bg != null) {
            s.log.append("  → 应用背景: ").append(s.bgName).append(" choose=").append(bg.getChoose()).append("\n");
            queueChooses(bg.getChoose(), s);
        }
    }

    private String labelForChooseItem(String item) {
        if (!isNum(item)) return item;
        Choose ch = chooseMap.get(parseInt(item));
        if (ch == null) return item;
        if (ch.getExtend() != null && !ch.getExtend().isEmpty()) return ch.getExtend();
        return "选项(" + item + ")";
    }

    // ==================== Choose queue ====================

    private void queueChooses(String ids, Session s) {
        if (ids == null || ids.isEmpty()) return;
        s.log.append("  → 入队 choose IDs: ").append(ids).append("\n");
        for (String idStr : ids.split(",")) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Choose ch = chooseMap.get(id);
                if (ch == null) continue;
                s.chooseQueue.add(id);
            } catch (NumberFormatException ignored) {}
        }
    }

    // ==================== Filter / Check ====================

    private List<String> filter(String[] items, String extend, Session s) {
        List<String> result = new ArrayList<>();
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            if (s.content.contains(item)) continue;
            if (isNum(item)) {
                result.add(item);
                continue;
            }
            if (!checkB(item, s)) continue;
            result.add(item);
        }
        return result;
    }

    private boolean checkB(String item, Session s) {
        // Check skill before
        if (skillBeforeMap.containsKey(item) && !s.content.contains(skillBeforeMap.get(item))) return false;
        // Check tool before
        if (toolBeforeMap.containsKey(item) && !s.content.contains(toolBeforeMap.get(item))) return false;
        for (Feat f : allFeats) {
            if (f.getName().equals(item)) {
                if (f.getBspell() != null && f.getBspell() && !s.canSpell) return false;
                if (f.getBefore() != null && !s.content.contains(f.getBefore())) return false;
                if (f.getBchoose() != null && !s.content.isEmpty() && !checkBChoose(f.getBchoose(), s)) return false;
            }
        }
        for (Pray p : allPrays) {
            if (p.getName().equals(item) && p.getMo() != null && p.getMo() && !s.content.contains("魔能爆")) return false;
        }
        return true;
    }

    // bchoose: each ID must have at least one matching item in content
    private boolean checkBChoose(String bchooseIds, Session s) {
        if (bchooseIds == null || bchooseIds.isEmpty()) return true;
        for (String idStr : bchooseIds.split(",")) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Choose ch = chooseMap.get(id);
                if (ch != null && ch.getList() != null) {
                    boolean any = false;
                    for (String item : ch.getList().split(",")) {
                        item = item.trim();
                        if (s.content.contains(item)) { any = true; break; }
                        if (attrMap.containsKey(item)) {
                            int idx = attrMap.get(item).getId();
                            if (idx < s.attrs.length && s.attrs[idx] >= 13) { any = true; break; }
                        }
                    }
                    if (!any) return false;
                }
            } catch (NumberFormatException ignored) {}
        }
        return true;
    }

    private boolean checkCareer(Career c, Session s) {
        if (c.getBefore() != null && !c.getBefore().isEmpty()) {
            for (String req : c.getBefore().split(",")) {
                req = req.trim();
                if (attrMap.containsKey(req)) { if (s.attrs[attrMap.get(req).getId()] < 13) return false; }
                else if (!s.content.contains(req)) return false;
            }
        }
        if (c.getBchoose() != null && !c.getBchoose().isEmpty() && !s.content.isEmpty() && !checkBChoose(c.getBchoose(), s)) {
            return false;
        }
        return true;
    }

    private boolean campCompatible(String campMask, Session s) {
        if (campMask == null || s.campIdx == null) return true;
        return s.campIdx < campMask.length() && campMask.charAt(s.campIdx) == '1';
    }

    private int campIdxOf(String campName) {
        for (int i = 0; i < AL.length; i++) if (AL[i].equals(campName)) return i;
        return 4; // default: 绝对中立
    }

    // ==================== Name lists ====================

    private List<String> raceNames() { return allRaces.stream().map(Race::getName).collect(Collectors.toList()); }

    private List<String> subraceNames(Session s) {
        if (s.race == null || s.race.getSub() == null || s.race.getSub().isEmpty()) return null;
        Set<String> set = resolveIds(s.race.getSub());
        return allSubraces.stream().filter(sr -> set.contains(sr.getName())).map(Subrace::getName).collect(Collectors.toList());
    }

    private List<String> subcareerNames(Session s) {
        if (s.career == null || s.career.getSub() == null || s.career.getSub().isEmpty()) return null;
        Set<String> set = resolveIds(s.career.getSub());
        return allSubcareers.stream().filter(sc -> set.contains(sc.getName())).map(Subcareer::getName).collect(Collectors.toList());
    }

    private Set<String> resolveIds(String ids) {
        Set<String> result = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) return result;
        for (String idStr : ids.split(",")) {
            Choose ch = chooseMap.get(parseInt(idStr));
            if (ch != null && ch.getList() != null)
                for (String item : ch.getList().split(",")) { item = item.trim(); if (!item.isEmpty() && !isNum(item)) result.add(item); }
        }
        return result;
    }

    // ==================== Final build ====================

    private WizardStepResponse buildFinal(Session s) {
        try {
            s.log.append("\n--- 最终角色 ---\n");
            s.log.append("  最终属性: ").append(Arrays.toString(s.attrs)).append(" 总和").append(sum(s.attrs)).append("\n");
            s.log.append("  最终内容(").append(s.content.size()).append("项): ").append(new ArrayList<>(s.content)).append("\n");
            int careerMoney = s.moneyValue != null ? s.moneyValue : DiceParser.rollMoney(s.career.getMoney());
            // Find life level from content: take the first matching money name
            int bgMoney = 0;
            String liveLevel = "乞食";
            for (String c : s.content) {
                Money m = moneyMap.get(c);
                if (m != null) { bgMoney = m.getInit(); liveLevel = m.getName(); break; }
            }
            int money = careerMoney + bgMoney;
            if (bgMoney > 0) s.log.append("  背景金钱: +").append(bgMoney).append(" GP\n");
            s.log.append("  金钱: ").append(s.career.getMoney()).append(" → 职业掷骰=").append(careerMoney).append(" GP 总计=").append(money).append(" GP\n");
            s.log.append("  生活等级: ").append(liveLevel).append(" (金钱").append(money).append(")\n");
            
            String camp = s.campIdx != null ? AL[s.campIdx] : s.content.stream().filter(c -> campOptions(s.campMask).contains(c)).findFirst().orElse("绝对中立");
            s.log.append("  最终阵营: ").append(camp).append("\n");

            String name = composeName(s);
            String age = s.ageYears != null ? (s.ageYears + "," + (s.ageMonths != null ? s.ageMonths : 0)) : "";
            
            Hero h = new Hero();
            h.setId(heroRepo.getMaxId() + 1);
            h.setName(name); h.setSex(s.sex);
            h.setRace(s.subrace != null ? s.subrace.getName() : (s.race != null ? s.race.getName() : ""));
            h.setCareer(s.subcareer != null ? s.subcareer.getName() : (s.career != null ? s.career.getName() : ""));
            h.setAttribute(join(s.attrs)); h.setSum(sum(s.attrs));
            h.setCamp(camp); h.setMoney(money);
            h.setLive(liveLevel);
            h.setSave(""); h.setAge(age);
            s.log.append("  姓名: ").append(name).append(" 年龄: ").append(age).append("\n");
            classifyContent(s.content, h);
            heroRepo.save(h);
            s.log.append("  保存成功 ID=").append(h.getId()).append("\n");
            saveLog(s, true, h.getId());
            
            HeroCard card = toCard(h);
            WizardStepResponse r = new WizardStepResponse();
            r.setSessionId(s.id); r.setStepType("final"); r.setStepTitle("角色创建完成");
            r.setDescription("角色创建成功！"); r.setPreview(card);
            r.setRolledAttributes(s.attrs); r.setAttrsSum(sum(s.attrs));
            r.setPreviewContent(new ArrayList<>(s.content));
            return r;
        } catch (Exception e) {
            s.log.append("  异常: ").append(e.getMessage()).append("\n");
            saveLog(s);
            return err(e.getMessage());
        }
    }

    private void saveLog(Session s) { saveLog(s, false, null); }
    private void saveLog(Session s, boolean success, Integer heroId) {
        try {
            java.io.File dir = new java.io.File("../logs");
            if (!dir.exists()) dir.mkdirs();
            String filename;
            if (heroId != null) {
                filename = "../logs/hero_" + heroId + "_" + (success ? "success" : "fail") + ".txt";
            } else {
                filename = "../logs/wizard_" + s.id + "_fail.txt";
            }
            try (java.io.FileWriter fw = new java.io.FileWriter(filename)) {
                fw.write(s.log.toString());
            }
            log.info("日志已保存: " + filename);
        } catch (Exception ex) {
            log.warn("日志保存失败: {}", ex.toString());
        }
    }

    private HeroCard toCard(Hero h) {
        HeroCard c = new HeroCard();
        c.setId(h.getId()); c.setName(h.getName()); c.setSex(h.getSex());
        c.setRace(h.getRace()); c.setCareer(h.getCareer()); c.setSum(h.getSum()); c.setCamp(h.getCamp());
        c.setAttributes(parseA(h.getAttribute()));
        c.setSkills(sl(h.getSkill())); c.setTools(sl(h.getTool())); c.setEquips(sl(h.getEquip()));
        c.setLanguages(sl(h.getLanguage())); c.setFeats(sl(h.getFeat()));
        c.setSave(h.getSave()); c.setMoney(h.getMoney()); c.setPray(h.getPray());
        c.setStyle(h.getStyle()); c.setSuperspell(h.getSuperspell()); c.setFight(h.getFight());
        c.setOther(h.getOther()); c.setAge(h.getAge()); c.setLive(h.getLive());
        c.setSpells0(sl(h.getSpell0())); c.setSpells1(sl(h.getSpell1())); c.setSpells2(sl(h.getSpell2()));
        return c;
    }

    private void classifyContent(Set<String> content, Hero h) {
        List<String> sk = new ArrayList<>(), tl = new ArrayList<>(), eq = new ArrayList<>(), ln = new ArrayList<>(),
            ft = new ArrayList<>(), sv = new ArrayList<>(), s0 = new ArrayList<>(), s1 = new ArrayList<>(), s2 = new ArrayList<>();
        String pr = null, st = null, ss = null, fi = null;
        List<String> ot = new ArrayList<>();
        for (String item : content) {
            // Skip money/life-level entries (stored in hero.live field separately)
            if (moneyMap.containsKey(item)) continue;
            String tbl = classifyMap.get(item);
            if (tbl == null) { ot.add(item); continue; }
            switch (tbl) {
                case "skill": sk.add(item); break; case "tool": tl.add(item); break; case "equip": eq.add(item); break;
                case "language": ln.add(item); break; case "feat": ft.add(item); break; case "save": sv.add(item); break;
                case "spell": { Integer lv = spellLevels.getOrDefault(item, -1);
                    if (lv == 0) s0.add(item); else if (lv == 1) s1.add(item); else if (lv == 2) s2.add(item); else ot.add(item); break; }
                case "pray": pr = pr == null ? item : pr + "," + item; break;
                case "style": st = st == null ? item : st + "," + item; break;
                case "superspell": ss = ss == null ? item : ss + "," + item; break;
                case "fight": fi = item; break;
                default: ot.add(item); break;
            }
        }
        h.setSkill(trunc(String.join(",", sk), 100)); h.setTool(trunc(String.join(",", tl), 300));
        h.setEquip(trunc(String.join(",", eq), 300)); h.setLanguage(trunc(String.join(",", ln), 300));
        h.setFeat(trunc(String.join(",", ft), 300)); h.setSave(trunc(String.join(",", sv), 50));
        h.setSpell0(trunc(String.join(",", s0), 300)); h.setSpell1(trunc(String.join(",", s1), 300));
        h.setSpell2(trunc(String.join(",", s2), 300));
        h.setPray(pr); h.setStyle(st); h.setSuperspell(ss); h.setFight(fi); h.setOther(trunc(String.join(",", ot), 500));
    }

    // ==================== Misc ====================

    private Race findRace(String n) { return allRaces.stream().filter(r -> r.getName().equals(n)).findFirst().orElse(null); }
    private Subrace findSubrace(String n) { return allSubraces.stream().filter(x -> x.getName().equals(n)).findFirst().orElse(null); }
    private Career findCareer(String n) { return allCareers.stream().filter(c -> c.getName().equals(n)).findFirst().orElse(null); }
    private Subcareer findSubcareer(String n) { return allSubcareers.stream().filter(x -> x.getName().equals(n)).findFirst().orElse(null); }
    private boolean isNum(String s) { try { Integer.parseInt(s); return true; } catch (NumberFormatException e) { return false; } }
    private int parseInt(String s) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return -1; } }
    private String attrStr(int[] a) { return Arrays.stream(a).mapToObj(String::valueOf).collect(Collectors.joining(",")); }
    private int sum(int[] a) { return Arrays.stream(a).sum(); }
    private String join(int[] a) { return String.join(",", Arrays.stream(a).mapToObj(String::valueOf).toArray(String[]::new)); }
    private List<Integer> ints(int[] a) { return Arrays.stream(a).boxed().collect(Collectors.toList()); }
    private String trunc(String s, int max) { if (s == null || s.isEmpty()) return ""; return s.length() <= max ? s : s.substring(0, max); }
    private List<Integer> parseA(String s) { if (s == null) return Arrays.asList(0,0,0,0,0,0); return Arrays.stream(s.split(",")).map(Integer::parseInt).collect(Collectors.toList()); }
    private List<String> sl(String s) { if (s == null || s.isEmpty()) return new ArrayList<>(); return Arrays.asList(s.split(",")); }

    private String composeName(Session s) {
        if (s.nameFirst == null) return genName(s);
        String nm = s.nameFirst;
        if (s.nameLast != null) nm += "·" + s.nameLast;
        if (s.nameAlias != null) nm += "·" + s.nameAlias;
        return nm;
    }

    private String genName(Session s) {
        if (allNames.isEmpty()) return "无名";
        NameTable nt = allNames.get(new Random().nextInt(allNames.size()));
        String[] f = "男".equals(s.sex) ? nt.getManName().split(",") : nt.getWomanName().split(",");
        String nm = f[new Random().nextInt(f.length)].trim();
        if (nt.getLname() != null && !nt.getLname().isEmpty())
            nm += "·" + nt.getLname().split(",")[new Random().nextInt(nt.getLname().split(",").length)].trim();
        if (nt.getAlias() != null && !nt.getAlias().isEmpty())
            nm += "·" + nt.getAlias().split(",")[new Random().nextInt(nt.getAlias().split(",").length)].trim();
        return nm;
    }

    private List<String> raceNameOptions(Session s) {
        List<String> opts = new ArrayList<>();
        if (s.race != null && s.race.getRaceName() != null && !s.race.getRaceName().isEmpty()) {
            for (String idStr : s.race.getRaceName().split(",")) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    Choose ch = chooseMap.get(id);
                    if (ch != null && ch.getList() != null) {
                        for (String item : ch.getList().split(",")) {
                            String t = item.trim();
                            if (!t.isEmpty()) opts.add(t);
                        }
                    }
                } catch (NumberFormatException e) {}
            }
        }
        return opts;
    }

    /**
     * If given item is a numeric choose ID, return its sub-list; otherwise null.
     */
    private List<String> subNameOptions(String item) {
        try {
            int id = Integer.parseInt(item.trim());
            Choose ch = chooseMap.get(id);
            if (ch != null && ch.getList() != null) {
                List<String> opts = new ArrayList<>();
                for (String t : ch.getList().split(",")) {
                    String tt = t.trim();
                    if (!tt.isEmpty()) opts.add(tt);
                }
                return opts;
            }
        } catch (NumberFormatException e) {}
        return null;
    }

    private void resolveNameTableByRace(Session s, String raceName) {
        for (NameTable n : allNames) { if (n.getName().equals(raceName)) { s.nameTable = n; return; } }
        if (!allNames.isEmpty()) s.nameTable = allNames.get(0);
    }

    private List<String> firstNameOptions(Session s) {
        List<String> opts = new ArrayList<>();
        if (s.nameTable == null) { opts.add("无名"); return opts; }
        String csv = "男".equals(s.sex) ? s.nameTable.getManName() : s.nameTable.getWomanName();
        if (csv != null) for (String item : csv.split(",")) { String t = item.trim(); if (!t.isEmpty()) opts.add(t); }
        if (opts.isEmpty()) opts.add("无名");
        return opts;
    }

    private List<String> lastNameOptions(Session s) {
        List<String> opts = new ArrayList<>();
        if (s.nameTable == null || s.nameTable.getLname() == null || s.nameTable.getLname().isEmpty()) return opts;
        for (String item : s.nameTable.getLname().split(",")) { String t = item.trim(); if (!t.isEmpty()) opts.add(t); }
        return opts;
    }

    private List<String> aliasOptions(Session s) {
        List<String> opts = new ArrayList<>();
        if (s.nameTable == null || s.nameTable.getAlias() == null || s.nameTable.getAlias().isEmpty()) return opts;
        for (String item : s.nameTable.getAlias().split(",")) { String t = item.trim(); if (!t.isEmpty()) opts.add(t); }
        return opts;
    }

    private String genAge(Subrace sr) {
        if (sr == null || sr.getAge() == null) return "";
        String[] p = sr.getAge().split(","); if (p.length < 2) return "";
        int a = parseInt(p[0]), b = parseInt(p[1]);
        int min = (int)(0.75*a), max = (int)(0.5*b);
        if (min >= max) return min + "," + new Random().nextInt(12);
        int age = min + new Random().nextInt(max-min+1);
        return age + "," + (age < max ? new Random().nextInt(12) : 0);
    }

    private String calcCamp(Session s) {
        String r = "111111111";
        s.log.append("  → 阵营计算: 初始=111111111\n");
        if (s.career != null && s.career.getCamp() != null) {
            s.log.append("    career(").append(s.career.getName()).append(") camp=").append(s.career.getCamp()).append("\n");
            r = campAnd(r, s.career.getCamp());
            s.log.append("    → AND结果=").append(r).append("\n");
        }
        if (s.subcareer != null && s.subcareer.getCamp() != null) {
            s.log.append("    subcareer(").append(s.subcareer.getName()).append(") camp=").append(s.subcareer.getCamp()).append("\n");
            r = campAnd(r, s.subcareer.getCamp());
            s.log.append("    → AND结果=").append(r).append("\n");
        }
        if (s.subrace != null && s.subrace.getCamp() != null) {
            s.log.append("    subrace(").append(s.subrace.getName()).append(") camp=").append(s.subrace.getCamp()).append("\n");
            r = campAnd(r, s.subrace.getCamp());
            s.log.append("    → AND结果=").append(r).append("\n");
        }
        List<Integer> ones = new ArrayList<>();
        for (int i = 0; i < r.length(); i++) if (r.charAt(i) == '1') ones.add(i);
        String[] AL = {"守序善良","中立善良","混乱善良","守序中立","绝对中立","混乱中立","守序邪恶","中立邪恶","混乱邪恶"};
        String selected = ones.isEmpty() ? "绝对中立" : AL[ones.get(new Random().nextInt(ones.size()))];
        s.log.append("  → 可用阵营位置(1-index): ").append(ones.isEmpty() ? "[无]" : ones.stream().map(o -> String.valueOf(o+1)).collect(Collectors.toList())).append("\n");
        s.log.append("  → 随机选定阵营: ").append(selected).append("\n");
        return selected;
    }
    private String campAnd(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return a;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++) sb.append(a.charAt(i)=='1' && b.charAt(i)=='1' ? '1' : '0');
        return sb.toString();
    }

    // ==================== Response builders ====================

    private WizardStepResponse toResponse(Session s) {
        if (s.error != null) { saveLog(s); return err(s.error); }
        if (s.finalized) return s.lastFinalResp;
        if (s.pendingStep == null) { s.error = "无可用步骤"; return err("无可用步骤"); }
        WizStep ws = s.pendingStep;
        WizardStepResponse r = new WizardStepResponse();
        r.setSessionId(s.id);
        r.setStepType(ws.type);
        r.setStepTitle(ws.title);
        r.setSelectCount(ws.cnt);
        r.setCandidates(ws.cands);
        r.setAttrsOptions(ws.attrOpts);
        r.setRolledAttributes(s.attrs.clone());
        r.setAttrsSum(sum(s.attrs));
        r.setPreviewContent(new ArrayList<>(s.content));
        // Attach age range for frontend validation
        if ("pick_age".equals(ws.type) && s.subrace != null) {
            int[] range = ageRange(s.subrace);
            if (range[0] > 0) { r.setAgeMin(range[0]); r.setAgeMax(range[1]); }
        }
        return r;
    }

    private WizardStepResponse respond(Session s, WizStep ws) {
        s.pendingStep = ws;
        return toResponse(s);
    }

    private WizardStepResponse err(String msg) { WizardStepResponse r = new WizardStepResponse(); r.setStepType("error"); r.setDescription(msg); return r; }

    // ==================== Inner classes ====================

    private static class Session {
        String id, formula, bgName, sex, mode = "roll";
        int step = STEP_ATTRS;
        int[] attrs = new int[6];
        List<int[]> attrPool = new ArrayList<>();
        boolean attrSetDone;
        String attrsInput;
        Set<String> content = new LinkedHashSet<>();
        boolean canSpell, bgApplied, subDone, scDone, sexPicked, finalized;
        com.dndhero.model.dto.WizardStepResponse lastFinalResp;
        boolean namePicked, campPicked, agePicked;
        boolean nameRacePicked, nameFirstPicked, nameLastPicked, nameAliasPicked;
        boolean moneyPicked; Integer moneyValue;
        String nameRace, nameFirst, nameLast, nameAlias;
        NameTable nameTable;
        String campMask = "111111111";
        Integer campIdx; // 0-8 index of chosen alignment
        Integer ageYears, ageMonths;
        String heroName;
        Race race; Subrace subrace; Career career; Subcareer subcareer;
        Deque<Integer> chooseQueue = new ArrayDeque<>();
        WizStep pendingStep;
        Map<String,String> labelToRaw = new HashMap<>();
        Map<String,Integer> dupeIndex = new HashMap<>(); // track duplicate labels
        StringBuilder log = new StringBuilder();
        String error;
        void add(String item) { if (item != null && !item.trim().isEmpty()) content.add(item.trim()); }
        void addAll(String csv) { if (csv != null) for (String a : csv.split(",")) { a = a.trim(); if (!a.isEmpty()) content.add(a); } }
        /** Resolve a picked label back to raw ID, or return as-is */
        String resolvePick(String p) { return labelToRaw.getOrDefault(p, p); }
    }

    private static class WizStep {
        String type, title; List<String> cands; int cnt; List<List<Integer>> attrOpts;
        WizStep(String t, String ti, List<String> c, int n, List<List<Integer>> a) { type = t; title = ti; cands = c; cnt = n; attrOpts = a; }
    }
}
