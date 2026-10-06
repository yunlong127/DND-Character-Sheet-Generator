package com.dndhero.service;

import com.dndhero.model.dto.*;
import com.dndhero.model.entity.*;
import com.dndhero.repository.*;
import com.dndhero.util.DiceParser;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CharacterGenerationService {

    private final ChooseRepository chooseRepo;
    private final RaceRepository raceRepo;
    private final SubraceRepository subraceRepo;
    private final CareerRepository careerRepo;
    private final SubcareerRepository subcareerRepo;
    private final BackgroundRepository bgRepo;
    private final NameTableRepository nameRepo;
    private final HeroRepository heroRepo;
    private final AttributeRepository attrRepo;
    private final FeatRepository featRepo;
    private final PrayRepository prayRepo;
    private final MoneyRepository moneyRepo;
    private final SkillRepository skillRepo;
    private final EquipRepository equipRepo;
    private final ToolRepository toolRepo;
    private final LanguageRepository langRepo;
    private final SaveRepository saveRepo;
    private final SpellRepository spellRepo;
    private final StyleRepository styleRepo;
    private final SuperspellRepository superspellRepo;
    private final FightRepository fightRepo;
    private final InformationRepository infoRepo;

    @Value("${app.log-dir:../logs}")
    private String logDir;

    @Value("${app.max-career-retries:3}")
    private int maxCareerRetries;

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

    // Classification maps: item name -> table field name
    private Map<String, String> classifyMap = new HashMap<>();
    private Map<String, Style> styleMap = new HashMap<>();
    private Map<String, Pray> prayMap = new HashMap<>();
    private Map<String, Feat> featMap = new HashMap<>();
    private Map<String, Integer> spellLevels = new HashMap<>();
    private Map<String, String> skillBeforeMap = new HashMap<>();
    private Map<String, String> toolBeforeMap = new HashMap<>();

    public CharacterGenerationService(ChooseRepository chooseRepo, RaceRepository raceRepo,
            SubraceRepository subraceRepo, CareerRepository careerRepo,
            SubcareerRepository subcareerRepo, BackgroundRepository bgRepo,
            NameTableRepository nameRepo, HeroRepository heroRepo,
            AttributeRepository attrRepo, FeatRepository featRepo,
            PrayRepository prayRepo, MoneyRepository moneyRepo,
            SkillRepository skillRepo, EquipRepository equipRepo,
            ToolRepository toolRepo, LanguageRepository langRepo,
            SaveRepository saveRepo, SpellRepository spellRepo,
            StyleRepository styleRepo, SuperspellRepository superspellRepo,
            FightRepository fightRepo, InformationRepository infoRepo) {
        this.chooseRepo = chooseRepo;
        this.raceRepo = raceRepo;
        this.subraceRepo = subraceRepo;
        this.careerRepo = careerRepo;
        this.subcareerRepo = subcareerRepo;
        this.bgRepo = bgRepo;
        this.nameRepo = nameRepo;
        this.heroRepo = heroRepo;
        this.attrRepo = attrRepo;
        this.featRepo = featRepo;
        this.prayRepo = prayRepo;
        this.moneyRepo = moneyRepo;
        this.skillRepo = skillRepo;
        this.equipRepo = equipRepo;
        this.toolRepo = toolRepo;
        this.langRepo = langRepo;
        this.saveRepo = saveRepo;
        this.spellRepo = spellRepo;
        this.styleRepo = styleRepo;
        this.superspellRepo = superspellRepo;
        this.fightRepo = fightRepo;
        this.infoRepo = infoRepo;
    }

    @PostConstruct
    public void init() {
        chooseMap = chooseRepo.findAll().stream().collect(Collectors.toMap(Choose::getId, c -> c));
        attrMap = attrRepo.findAll().stream().collect(Collectors.toMap(Attribute::getName, a -> a));
        allRaces = raceRepo.findAll();
        allSubraces = subraceRepo.findAll();
        allCareers = careerRepo.findAll();
        allSubcareers = subcareerRepo.findAll();
        bgMap = bgRepo.findAll().stream().collect(Collectors.toMap(Background::getName, b -> b));
        allNames = nameRepo.findAll();
        allFeats = featRepo.findAll();
        allFeats.forEach(f -> featMap.put(f.getName(), f));
        allPrays = prayRepo.findAll();
        moneyMap = moneyRepo.findAll().stream().collect(Collectors.toMap(Money::getName, m -> m));

        // Build classification maps
        skillRepo.findAll().forEach(s -> { classifyMap.put(s.getName(), "skill"); if (s.getBefore() != null && !s.getBefore().isEmpty()) skillBeforeMap.put(s.getName(), s.getBefore()); });
        equipRepo.findAll().forEach(e -> classifyMap.put(e.getName(), "equip"));
        toolRepo.findAll().forEach(t -> { classifyMap.put(t.getName(), "tool"); if (t.getBefore() != null && !t.getBefore().isEmpty()) toolBeforeMap.put(t.getName(), t.getBefore()); });
        langRepo.findAll().forEach(l -> classifyMap.put(l.getName(), "language"));
        featRepo.findAll().forEach(f -> classifyMap.put(f.getName(), "feat"));
        saveRepo.findAll().forEach(s -> classifyMap.put(s.getName(), "save"));
        spellRepo.findAll().forEach(s -> { classifyMap.put(s.getName(), "spell"); spellLevels.put(s.getName(), s.getLevel()); });
        allPrays.forEach(p -> { classifyMap.put(p.getName(), "pray"); prayMap.put(p.getName(), p); });
        styleRepo.findAll().forEach(s -> { classifyMap.put(s.getName(), "style"); styleMap.put(s.getName(), s); });
        superspellRepo.findAll().forEach(s -> classifyMap.put(s.getName(), "superspell"));
        fightRepo.findAll().forEach(f -> classifyMap.put(f.getName(), "fight"));
        infoRepo.findAll().forEach(i -> classifyMap.put(i.getName(), "other"));

        // Hardcoded fallback: items not in any table PK but known categories
        Set.of("通用语").forEach(s -> classifyMap.putIfAbsent(s, "language"));
        Set.of("轻甲","中甲","重甲","盾牌","简易","军用","火器","手弩","长剑","刺剑","短剑")
            .forEach(s -> classifyMap.putIfAbsent(s, "equip"));
    }

    public GenerateResponse generate(GenerateRequest request) {
        if (request.getCount() > 0) {
            return generateRandom(request);
        } else {
            return generateManual(request);
        }
    }

    public List<HeroCard> listHeroes(String sort, boolean asc, String search, Boolean favoriteOnly) {
        List<Hero> heroes = heroRepo.findAll();
        if (favoriteOnly != null && favoriteOnly) {
            heroes = heroes.stream().filter(h -> h.getFavorite() != null && h.getFavorite()).collect(Collectors.toList());
        }
        List<HeroCard> cards = heroes.stream().map(this::toHeroCard).collect(Collectors.toList());

        // Filter by search (name/race/career)
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            cards = cards.stream().filter(h ->
                    (h.getName() != null && h.getName().toLowerCase().contains(s)) ||
                    (h.getRace() != null && h.getRace().toLowerCase().contains(s)) ||
                    (h.getCareer() != null && h.getCareer().toLowerCase().contains(s))
            ).collect(Collectors.toList());
        }

        // Sort
        if (sort != null) {
            Comparator<HeroCard> comp = switch (sort) {
                case "sum" -> Comparator.comparing(HeroCard::getSum);
                case "money" -> Comparator.comparing(HeroCard::getMoney);
                case "age" -> Comparator.comparing(h -> {
                    try { return Integer.parseInt(h.getAge().split(",")[0]); }
                    catch (Exception e) { return 0; }
                });
                case "str" -> Comparator.comparing(h -> h.getAttributes().get(0));
                case "dex" -> Comparator.comparing(h -> h.getAttributes().get(1));
                case "con" -> Comparator.comparing(h -> h.getAttributes().get(2));
                case "int" -> Comparator.comparing(h -> h.getAttributes().get(3));
                case "wis" -> Comparator.comparing(h -> h.getAttributes().get(4));
                case "cha" -> Comparator.comparing(h -> h.getAttributes().get(5));
                default -> Comparator.comparing(HeroCard::getSum);
            };
            if (!asc) comp = comp.reversed();
            cards.sort(comp);
        }

        return cards;
    }

    public Map<String, Object> getHeroStats(String search) {
        List<Hero> heroes = heroRepo.findAll();
        List<HeroCard> cards = heroes.stream().map(this::toHeroCard).collect(Collectors.toList());

        // Filter by search
        if (search != null && !search.isEmpty()) {
            String s = search.toLowerCase();
            cards = cards.stream().filter(h ->
                    (h.getName() != null && h.getName().toLowerCase().contains(s)) ||
                    (h.getRace() != null && h.getRace().toLowerCase().contains(s)) ||
                    (h.getCareer() != null && h.getCareer().toLowerCase().contains(s))
            ).collect(Collectors.toList());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stats", calculateStats(cards));
        return result;
    }

    public HeroCard toggleFavorite(Integer id) {
        Hero hero = heroRepo.findById(id).orElse(null);
        if (hero == null) return null;
        hero.setFavorite(hero.getFavorite() == null || !hero.getFavorite());
        heroRepo.save(hero);
        return toHeroCard(hero);
    }

    // ==================== Random Generation ====================

    private GenerateResponse generateRandom(GenerateRequest request) {
        int targetCount = request.getCount();
        int successCount = 0;
        int failCount = 0;
        int globalAttempt = 0;
        List<HeroCard> heroCards = new ArrayList<>();

        int maxAttempts = targetCount * 50;
        while (successCount < targetCount && globalAttempt < maxAttempts) {
            globalAttempt++;
            boolean savedHero = false;
            Integer heroId = null;
            StringBuilder log = new StringBuilder();
            log.append("=== 角色生成日志 (第").append(globalAttempt).append("次尝试) ===\n");

            try {
                // Step 1: roll attributes
                log.append("步骤1: 掷属性 公式=").append(request.getFormula()).append("\n");
                int[] attributes = new int[6];
                for (int j = 0; j < 6; j++) attributes[j] = DiceParser.roll(request.getFormula());
                log.append("  属性: ").append(Arrays.toString(attributes)).append("\n");

                // Step 2: random race
                Race race = allRaces.get(new Random().nextInt(allRaces.size()));
                log.append("步骤2: 随机种族=").append(race.getName()).append("\n");
                Set<String> content = new LinkedHashSet<>();
                boolean canSpell = false;
                resolveChooseIdList(race.getChoose(), content, attributes, canSpell, log, "  ");
                addAbility(race.getAbility(), content, log, "  ");
                addSkillOrAbility(race.getSkill(), content, log, "  ");

                // Step 3: pick subrace
                Subrace subrace = pickSubrace(race, content, attributes, canSpell, log);
                if (subrace == null) {
                    log.append("步骤3失败: 没有符合条件的亚种, 销毁当前角色\n");
                    saveLog(globalAttempt, log.toString(), false);
                    failCount++;
                    continue;
                }
                log.append("  亚种=").append(subrace.getName()).append("\n");
                applyAttributeBonus(subrace.getAttribute(), attributes, log, "  ");
                addAbility(subrace.getAbility(), content, log, "  ");
                addSkillOrAbility(subrace.getSkill(), content, log, "  ");
                if (subrace.getCanSpell() != null && subrace.getCanSpell()) canSpell = true;

                // Step 4: random sex
                String sex = new Random().nextBoolean() ? "男" : "女";
                log.append("步骤4: 随机性别=").append(sex).append("\n");

                // Step 5: generate name (after sex is known)
                String heroName = generateName(sex, race, log);
                log.append("步骤5: 姓名=").append(heroName).append("\n");

                // Step 6: random age
                String age = generateAge(subrace);
                log.append("步骤6: 年龄=").append(age).append("\n");

                // Step 7: camp from subrace
                String camp = calculateCamp(subrace, log, "  ");
                log.append("  最终阵营: ").append(camp).append("\n");

                // Step 8: apply background
                log.append("步骤8: 应用背景=").append(request.getBackground()).append("\n");
                Background bg = bgMap.get(request.getBackground());
                if (bg != null) {
                    resolveChooseIdList(bg.getChoose(), content, attributes, canSpell, log, "  ");
                }

                // Step 9: random career with camp check + retries
                log.append("步骤9: 随机职业\n");
                Career career = pickCareer(content, attributes, canSpell, subrace, camp, log);
                if (career == null) {
                    log.append("步骤9失败: 达到最大重试次数, 销毁当前角色\n");
                    saveLog(globalAttempt, log.toString(), false);
                    failCount++;
                    continue;
                }
                log.append("  职业=").append(career.getName()).append("\n");
                addAbility(career.getAbility(), content, log, "  ");
                addSkillOrAbility(career.getSkill(), content, log, "  ");
                if (career.getCanSpell() != null && career.getCanSpell()) canSpell = true;
                resolveChooseIdList(career.getChoose(), content, attributes, canSpell, log, "  ");

                // Pick subcareer
                Subcareer subcareer = pickSubcareer(career, content, attributes, canSpell, camp, log);
                if (subcareer != null) {
                    log.append("  子职=").append(subcareer.getName()).append("\n");
                    addAbility(subcareer.getAbility(), content, log, "  ");
                    addSkillOrAbility(subcareer.getSkill(), content, log, "  ");
                    resolveChooseIdList(subcareer.getChoose(), content, attributes, canSpell, log, "  ");
                }

                // Step 10: calculate money
                int baseMoney = DiceParser.rollMoney(career.getMoney());
                log.append("  金钱: ").append(career.getMoney()).append(" → 掷骰=").append(baseMoney).append(" GP\n");
                int bgMoney = getBgMoney(content);
                int money = baseMoney + bgMoney;
                String lifeLevel = getLifeLevelFromContent(content);
                if (bgMoney > 0) log.append("  背景金钱: +").append(bgMoney).append(" GP\n");
                log.append("  最终金钱: ").append(money).append(" GP 生活等级=").append(lifeLevel).append("\n");

                // Build hero
                Hero hero = new Hero();
                hero.setId(heroRepo.getMaxId() + 1);
                heroId = hero.getId();
                hero.setName(heroName);
                hero.setSex(sex);
                hero.setRace(subrace.getName());
                hero.setCareer(subcareer != null ? subcareer.getName() : career.getName());
                hero.setAttribute(joinAttributes(attributes));
                hero.setSum(calcSum(attributes));
                hero.setCamp(camp);
                hero.setMoney(money);
                hero.setLive(lifeLevel);
                hero.setSave("");
                hero.setAge(age);

                classifyContent(content, hero);
                heroRepo.save(hero);
                savedHero = true;
                HeroCard card = toHeroCard(hero);
                heroCards.add(card);

                log.append("--- 角色汇总 ---\n");
                log.append("  姓名=").append(heroName).append(" 性别=").append(sex).append("\n");
                log.append("  种族=").append(hero.getRace()).append(" 职业=").append(hero.getCareer()).append("\n");
                log.append("  属性=").append(hero.getAttribute()).append(" 总和=").append(hero.getSum()).append("\n");
                log.append("  阵营=").append(camp).append(" 金钱=").append(money).append(" GP\n");
                log.append("  内容(").append(content.size()).append("项): ").append(new ArrayList<>(content)).append("\n");
                log.append("角色卡生成成功, 保存ID=").append(hero.getId()).append("\n");
                saveLog(hero.getId(), log.toString(), true);
                successCount++;

            } catch (Exception e) {
                log.append("异常: ").append(e.getMessage()).append(", 销毁当前角色\n");
                int logId = savedHero && heroId != null ? heroId : globalAttempt;
                saveLog(logId, log.toString(), false);
                failCount++;
            }
        }

        GenerateResponse resp = new GenerateResponse();
        resp.setSuccess(successCount > 0);
        resp.setMessage(String.format("成功:%d 失败:%d (共尝试%d次)", successCount, failCount, globalAttempt));
        resp.setHeroes(heroCards);
        resp.setStats(calculateStats(heroCards));
        if (!heroCards.isEmpty()) {
            resp.setHero(heroCards.get(heroCards.size() - 1));
            resp.setHeroId(heroCards.get(heroCards.size() - 1).getId());
        }
        return resp;
    }

    // ==================== Manual Generation ====================

    private GenerateResponse generateManual(GenerateRequest request) {
        GenerateResponse resp = new GenerateResponse();

        // List available careers and races
        resp.setAvailableCareers(allCareers.stream().map(Career::getName).collect(Collectors.toList()));
        resp.setAvailableRaces(allRaces.stream().map(Race::getName).collect(Collectors.toList()));

        if (request.getCareerName() == null || request.getRaceName() == null) {
            resp.setSuccess(true);
            resp.setMessage("请选择职业和种族");
            return resp;
        }

        StringBuilder log = new StringBuilder();
        log.append("=== 角色生成日志(手动) ===\n");
        int heroId = (int) (System.currentTimeMillis() % 100000);

        try {
            // Roll or use manual attributes
            int[] attributes = new int[6];
            if (request.getManualAttributes() != null && request.getManualAttributes().size() == 6) {
                for (int i = 0; i < 6; i++) attributes[i] = request.getManualAttributes().get(i);
            } else {
                for (int i = 0; i < 6; i++) attributes[i] = DiceParser.roll(request.getFormula());
            }
            log.append("属性: ").append(Arrays.toString(attributes)).append("\n");

            Set<String> content = new LinkedHashSet<>();
            boolean canSpell = false;

            // Race
            Race race = allRaces.stream().filter(r -> r.getName().equals(request.getRaceName())).findFirst().orElse(null);
            if (race == null) throw new RuntimeException("种族不存在");
            resolveChooseIdList(race.getChoose(), content, attributes, canSpell, log, "  ");
            addAbility(race.getAbility(), content, log, "  ");
            addSkillOrAbility(race.getSkill(), content, log, "  ");

            // Subrace
            Subrace subrace = request.getSubraceName() != null
                    ? allSubraces.stream().filter(s -> s.getName().equals(request.getSubraceName())).findFirst().orElse(null)
                    : pickSubrace(race, content, attributes, canSpell, log);
            if (subrace != null) {
                applyAttributeBonus(subrace.getAttribute(), attributes, log, "  ");
                addAbility(subrace.getAbility(), content, log, "  ");
                addSkillOrAbility(subrace.getSkill(), content, log, "  ");
                if (subrace.getCanSpell() != null && subrace.getCanSpell()) canSpell = true;
            }

            // Background
            Background bg = bgMap.get(request.getBackground());
            if (bg != null) {
                resolveChooseIdList(bg.getChoose(), content, attributes, canSpell, log, "  ");
            }

            // Career
            Career career = allCareers.stream().filter(c -> c.getName().equals(request.getCareerName())).findFirst().orElse(null);
            if (career == null) throw new RuntimeException("职业不存在");
            addAbility(career.getAbility(), content, log, "  ");
            addSkillOrAbility(career.getSkill(), content, log, "  ");
            if (career.getCanSpell() != null && career.getCanSpell()) canSpell = true;
            resolveChooseIdList(career.getChoose(), content, attributes, canSpell, log, "  ");

            // Subcareer
            Subcareer subcareer = request.getSubcareerName() != null
                    ? allSubcareers.stream().filter(s -> s.getName().equals(request.getSubcareerName())).findFirst().orElse(null)
                    : null;
            if (subcareer != null) {
                addAbility(subcareer.getAbility(), content, log, "  ");
                addSkillOrAbility(subcareer.getSkill(), content, log, "  ");
                resolveChooseIdList(subcareer.getChoose(), content, attributes, canSpell, log, "  ");
            }

            int money = DiceParser.rollMoney(career.getMoney());
            log.append("  金钱: ").append(career.getMoney()).append(" → 掷骰=").append(money).append(" GP\n");
            String heroName = generateName(request.getSex() != null ? request.getSex() : "男", race, log);

            Hero hero = new Hero();
            hero.setId(heroRepo.getMaxId() + 1);
            hero.setName(heroName);
            hero.setSex(request.getSex() != null ? request.getSex() : "男");
            hero.setRace(subrace != null ? subrace.getName() : race.getName());
            hero.setCareer(subcareer != null ? subcareer.getName() : career.getName());
            hero.setAttribute(joinAttributes(attributes));
            hero.setCamp(calculateCamp(career, subcareer, subrace, log, "  "));
            hero.setMoney(money);
            hero.setLive(getLiveLevel(money, log));
            hero.setSave("");
            hero.setAge(subrace != null ? generateAge(subrace) : "");
            classifyContent(content, hero);

            heroRepo.save(hero);
            resp.setHero(toHeroCard(hero));
            resp.setHeroId(hero.getId());
            resp.setSuccess(true);
            resp.setMessage("创建成功");

            saveLog(heroId, log.toString(), true);
        } catch (Exception e) {
            log.append("异常: ").append(e.getMessage()).append("\n");
            saveLog(heroId, log.toString(), false);
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    // ==================== Choose Resolution ====================

    /**
     * Resolve a comma-separated list of choose IDs.
     */
    private void resolveChooseIdList(String chooseIds, Set<String> content, int[] attributes,
            boolean canSpell, StringBuilder log, String indent) {
        if (chooseIds == null || chooseIds.isEmpty()) return;
        for (String idStr : chooseIds.split(",")) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Choose ch = chooseMap.get(id);
                if (ch != null) {
                    resolveChoose(ch, content, attributes, canSpell, log, indent);
                }
            } catch (NumberFormatException e) {
                // skip non-numeric
            }
        }
    }

    private void resolveChoose(Choose choose, Set<String> content, int[] attributes,
            boolean canSpell, StringBuilder log, String indent) {
        if (choose == null) return;

        String list = choose.getList();
        if (list == null || list.isEmpty()) return;

        String[] items = list.split(",");
        int cnt = choose.getCnt() != null ? choose.getCnt() : items.length;
        // cnt=-1 means "pick all"
        if (cnt < 0) cnt = items.length;

        // Build candidate list, trim whitespace
        List<String> candidates = new ArrayList<>();
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            candidates.add(item);
        }

        // Step 2: set subtraction - remove items already in content
        candidates = candidates.stream().filter(c -> !content.contains(c)).collect(Collectors.toList());

        // Step 3: filter by preconditions (bchoose, bspell, before, mo)
        candidates = candidates.stream().filter(c -> checkPreconditionsForItem(c, content, canSpell)).collect(Collectors.toList());

        // Step 4: Select up to cnt, randomly if more candidates than needed
        List<String> selected;
        if (candidates.size() <= cnt) {
            selected = new ArrayList<>(candidates);
        } else {
            List<String> shuffled = new ArrayList<>(candidates);
            Collections.shuffle(shuffled);
            selected = shuffled.subList(0, cnt);
        }

        for (String sel : selected) {
            processChosenItem(sel, content, attributes, canSpell, log, indent);
        }
        if (!selected.isEmpty()) {
            log.append(indent).append("choose(").append(choose.getId()).append(") \"").append(choose.getExtend())
                .append("\" 选中(").append(selected.size()).append("/").append(cnt).append("): ").append(selected).append("\n");
        }
    }

    private boolean checkPreconditionsForItem(String item, Set<String> content, boolean canSpell) {
        // Start with the passed canSpell; also check if content already contains a feat with canSpell=true
        if (!canSpell) {
            for (String c : content) {
                Feat fc = featMap.get(c);
                if (fc != null && fc.getCanSpell() != null && fc.getCanSpell()) { canSpell = true; break; }
                Pray pc = prayMap.get(c);
                if (pc != null && pc.getCanSpell() != null && pc.getCanSpell()) { canSpell = true; break; }
            }
        }
        try {
            Integer.parseInt(item);
            return true;
        } catch (NumberFormatException e) {
            // Check skill/tool before
            if (skillBeforeMap.containsKey(item) && !content.contains(skillBeforeMap.get(item))) return false;
            if (toolBeforeMap.containsKey(item) && !content.contains(toolBeforeMap.get(item))) return false;
            // Text item - check if any feat with before/bspell/bchoose matches this name
            for (Feat f : allFeats) {
                if (f.getName().equals(item)) {
                    if (f.getBspell() != null && f.getBspell() && !canSpell) return false;
                    if (f.getBefore() != null && !content.contains(f.getBefore())) return false;
                    if (f.getBchoose() != null && !checkBChoose(f.getBchoose(), content, canSpell)) return false;
                }
            }
        }
        return true;
    }

    private void processChosenItem(String item, Set<String> content, int[] attributes,
            boolean canSpell, StringBuilder log, String indent) {
        item = item.trim();
        if (item.isEmpty()) return;

        // Check if it's a number (choose ID reference)
        try {
            int id = Integer.parseInt(item);
            Choose ch = chooseMap.get(id);
            if (ch != null) {
                log.append(indent).append("处理 choose(").append(id).append(") extend=").append(ch.getExtend()).append("\n");
                if (ch.getChoose() != null && !ch.getChoose().isEmpty()) {
                    resolveChooseIdList(ch.getChoose(), content, attributes, canSpell, log, indent + "  ");
                }
                resolveChoose(ch, content, attributes, canSpell, log, indent + "  ");
            }
            return;
        } catch (NumberFormatException e) {
            // not a number, continue
        }

        // Check if it's an attribute name
        if (attrMap.containsKey(item)) {
            int idx = attrMap.get(item).getId();
            attributes[idx]++;
            log.append(indent).append("属性加成: ").append(item).append("+1\n");
        } else {
            if (content.add(item)) {
                log.append(indent).append("添加: ").append(item).append(" 内容:").append(content.size()).append("项\n");
                // Check if this is a style with cascading chooses
                Style st = styleMap.get(item);
                if (st != null && st.getChoose() != null && !st.getChoose().isEmpty()) {
                    log.append(indent).append("  → 风格 ").append(item).append(" 级联 choose=").append(st.getChoose()).append("\n");
                    resolveChooseIdList(st.getChoose(), content, attributes, canSpell, log, indent + "    ");
                }
                // Check if this is a pray with cascading chooses or ability
                Pray pr = prayMap.get(item);
                if (pr != null) {
                    if (pr.getAbility() != null) {
                        for (String a : pr.getAbility().split(",")) {
                            String aa = a.trim();
                            if (attrMap.containsKey(aa)) {
                                int idx = attrMap.get(aa).getId();
                                attributes[idx]++;
                                log.append(indent).append("   属性(").append(item).append("): ").append(aa).append("+1\n");
                            } else if (!aa.isEmpty() && content.add(aa)) {
                                log.append(indent).append("   祈祷(").append(item).append("): +").append(aa).append("\n");
                            }
                        }
                    }
                    if (pr.getChoose() != null && !pr.getChoose().isEmpty()) {
                        log.append(indent).append("  → 祈祷 ").append(item).append(" 级联 choose=").append(pr.getChoose()).append("\n");
                        resolveChooseIdList(pr.getChoose(), content, attributes, canSpell, log, indent + "    ");
                    }
                }
                // Check feat: ability bonus, canSpell, cascading chooses
                Feat ft = featMap.get(item);
                if (ft != null) {
                    if (ft.getAbility() != null) {
                        for (String a : ft.getAbility().split(",")) {
                            String aa = a.trim();
                            if (attrMap.containsKey(aa)) {
                                int idx = attrMap.get(aa).getId();
                                attributes[idx]++;
                                log.append(indent).append("   属性(").append(item).append("): ").append(aa).append("+1\n");
                            } else if (!aa.isEmpty() && content.add(aa)) {
                                log.append(indent).append("   专长(").append(item).append("): +").append(aa).append("\n");
                            }
                        }
                    }
                    if (ft.getChoose() != null && !ft.getChoose().isEmpty()) {
                        log.append(indent).append("  → 专长 ").append(item).append(" 级联 choose=").append(ft.getChoose()).append("\n");
                        resolveChooseIdList(ft.getChoose(), content, attributes, canSpell, log, indent + "    ");
                    }
                }
            }
        }
    }

    // ==================== Helper Methods ====================

    private Subrace pickSubrace(Race race, Set<String> content, int[] attributes,
            boolean canSpell, StringBuilder log) {
        if (race.getSub() == null || race.getSub().isEmpty()) return null;
        // race.sub is comma-separated IDs to choose table, resolves to subrace names
        Set<String> tmpContent = new LinkedHashSet<>();
        int[] tmpAttrs = Arrays.copyOf(attributes, attributes.length);
        resolveChooseIdList(race.getSub(), tmpContent, tmpAttrs, canSpell, log, "    ");
        // Copy tmp attributes back (subrace choose may add attributes)
        System.arraycopy(tmpAttrs, 0, attributes, 0, attributes.length);
        // The resolved names in tmpContent are subrace names
        List<Subrace> matching = allSubraces.stream()
                .filter(s -> tmpContent.contains(s.getName()))
                .collect(Collectors.toList());
        if (matching.isEmpty()) return null;
        return matching.get(new Random().nextInt(matching.size()));
    }

    private Career pickCareer(Set<String> content, int[] attributes, boolean canSpell,
            Subrace subrace, String selectedCamp, StringBuilder log) {
        List<Career> pool = new ArrayList<>(allCareers);
        java.util.Collections.shuffle(pool);
        int tries = 0;
        int maxTries = 3;
        for (Career career : pool) {
            if (tries >= maxTries) break;
            tries++;
            if (!checkCareerBefore(career, attributes, content)) {
                log.append("    职业 ").append(career.getName()).append(" 不满足前置条件\n");
                continue;
            }
            if (!checkBChoose(career.getBchoose(), content, canSpell, attributes)) {
                log.append("    职业 ").append(career.getName()).append(" 不满足bchoose\n");
                continue;
            }
            if (!campAllowsAlignment(career.getCamp(), selectedCamp)) {
                log.append("    职业 ").append(career.getName()).append(" 阵营不匹配(角色阵营:").append(selectedCamp).append(")\n");
                continue;
            }
            if (career.getSub() != null && !career.getSub().isEmpty()) {
                Set<String> subNames = new LinkedHashSet<>();
                for (String idStr : career.getSub().split(",")) {
                    try {
                        Choose ch = chooseMap.get(Integer.parseInt(idStr.trim()));
                        if (ch != null && ch.getList() != null) {
                            for (String item : ch.getList().split(",")) {
                                String t = item.trim();
                                if (!t.isEmpty()) subNames.add(t);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
                boolean hasValidSub = false;
                for (String sn : subNames) {
                    Subcareer sc = allSubcareers.stream().filter(s -> s.getName().equals(sn)).findFirst().orElse(null);
                    if (sc != null && campAllowsAlignment(sc.getCamp(), selectedCamp)) {
                        hasValidSub = true;
                        break;
                    }
                }
                if (!hasValidSub) {
                    log.append("    职业 ").append(career.getName()).append(" 子职阵营冲突\n");
                    continue;
                }
            }
            return career;
        }
        return null;
    }

    private boolean checkCareerBefore(Career career, int[] attributes, Set<String> content) {
        if (career.getBefore() == null || career.getBefore().isEmpty()) return true;
        String[] prereqs = career.getBefore().split(",");
        for (String req : prereqs) {
            req = req.trim();
            if (attrMap.containsKey(req)) {
                int idx = attrMap.get(req).getId();
                if (attributes[idx] < 13) return false;
            } else if (!content.contains(req)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkBChoose(String bchooseIds, Set<String> content, boolean canSpell) {
        return checkBChoose(bchooseIds, content, canSpell, new int[0]);
    }

    private boolean checkBChoose(String bchooseIds, Set<String> content, boolean canSpell, int[] attributes) {
        if (bchooseIds == null || bchooseIds.isEmpty()) return true;
        for (String idStr : bchooseIds.split(",")) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Choose ch = chooseMap.get(id);
                if (ch != null && ch.getList() != null) {
                    boolean anyMatch = false;
                    for (String item : ch.getList().split(",")) {
                        item = item.trim();
                        if (content.contains(item)) { anyMatch = true; break; }
                        if (attrMap.containsKey(item)) {
                            int idx = attrMap.get(item).getId();
                            if (idx < attributes.length && attributes[idx] >= 13) { anyMatch = true; break; }
                        }
                    }
                    if (!anyMatch) return false;
                }
            } catch (NumberFormatException ignored) {}
        }
        return true;
    }

    private Subcareer pickSubcareer(Career career, Set<String> content, int[] attributes,
            boolean canSpell, String selectedCamp, StringBuilder log) {
        if (career.getSub() == null || career.getSub().isEmpty()) return null;
        // Collect all possible subcareer names from the "sub" choose list (skip random selection)
        Set<String> subNames = new LinkedHashSet<>();
        for (String idStr : career.getSub().split(",")) {
            try {
                Choose ch = chooseMap.get(Integer.parseInt(idStr.trim()));
                if (ch != null && ch.getList() != null) {
                    for (String item : ch.getList().split(",")) {
                        String t = item.trim();
                        if (!t.isEmpty()) subNames.add(t);
                    }
                }
            } catch (NumberFormatException ignored) {}
        }
        log.append("    子职候选(").append(subNames.size()).append("): ").append(subNames).append("\n");
        List<Subcareer> valid = allSubcareers.stream()
                .filter(s -> subNames.contains(s.getName()) && campAllowsAlignment(s.getCamp(), selectedCamp))
                .collect(Collectors.toList());
        if (valid.isEmpty()) return null;
        return valid.get(new Random().nextInt(valid.size()));
    }

    private boolean campIntersects(String camp1, String camp2) {
        if (camp1 == null || camp2 == null) return true;
        if (camp1.length() != camp2.length()) return true;
        for (int i = 0; i < camp1.length(); i++) {
            if (camp1.charAt(i) == '1' && camp2.charAt(i) == '1') return true;
        }
        return false;
    }

    /**
     * Check if the given camp mask allows the specific selected alignment.
     * campMask is a 9-bit string; selectedCamp is one of the 9 ALIGNMENTS.
     */
    private boolean campAllowsAlignment(String campMask, String selectedCamp) {
        if (campMask == null || campMask.isEmpty() || selectedCamp == null) return true;
        for (int i = 0; i < ALIGNMENTS.length; i++) {
            if (ALIGNMENTS[i].equals(selectedCamp)) {
                return i < campMask.length() && campMask.charAt(i) == '1';
            }
        }
        return true;
    }

    private void applyAttributeBonus(String attrBonus, int[] attributes, StringBuilder log, String indent) {
        if (attrBonus == null || attrBonus.length() < 6) return;
        for (int i = 0; i < 6 && i < attrBonus.length(); i++) {
            char c = attrBonus.charAt(i);
            if (c >= '0' && c <= '9') {
                attributes[i] += (c - '0');
            }
        }
        log.append(indent).append("应用属性加成: ").append(attrBonus)
                .append(" -> ").append(Arrays.toString(attributes)).append("\n");
    }

    private void addAbility(String ability, Set<String> content, StringBuilder log, String indent) {
        if (ability == null || ability.isEmpty()) return;
        List<String> added = new ArrayList<>();
        for (String item : ability.split(",")) {
            item = item.trim();
            if (!item.isEmpty() && content.add(item)) added.add(item);
        }
        if (!added.isEmpty()) log.append(indent).append("添加能力: ").append(added).append(" 内容:").append(content.size()).append("项\n");
    }

    private void addSkillOrAbility(String skill, Set<String> content, StringBuilder log, String indent) {
        if (skill == null || skill.isEmpty()) return;
        List<String> added = new ArrayList<>();
        for (String item : skill.split(",")) {
            item = item.trim();
            if (!item.isEmpty() && content.add(item)) added.add(item);
        }
        if (!added.isEmpty()) log.append(indent).append("添加技能: ").append(added).append(" 内容:").append(content.size()).append("项\n");
    }

    private String generateAge(Subrace subrace) {
        if (subrace == null || subrace.getAge() == null) return "";
        String[] parts = subrace.getAge().split(",");
        if (parts.length < 2) return "";
        try {
            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());
            int min = (int)(0.75 * a);
            int max = (int)(0.5 * b);
            if (min >= max) return min + "," + new Random().nextInt(12);
            int age = min + new Random().nextInt(max - min + 1);
            // month 0-11, but at max age, month cannot exceed 0 (bound: max,0)
            int month = age < max ? new Random().nextInt(12) : 0;
            return age + "," + month;
        } catch (NumberFormatException e) {
            return "";
        }
    }

    private String generateName(String sex, Race race, StringBuilder log) {
        if (allNames.isEmpty()) return "无名";

        NameTable nt = null;
        if (race.getRaceName() != null && !race.getRaceName().isEmpty()) {
            // Multi-level random walk: pick one child at each level, ending at a leaf name
            String chosen = randomWalkChooseChain(race.getRaceName().split(","));
            if (chosen != null) {
                for (NameTable n : allNames) {
                    if (n.getName().equals(chosen)) { nt = n; break; }
                }
            }
        }
        if (nt == null) nt = allNames.get(new Random().nextInt(allNames.size()));

        String[] firstNames = "男".equals(sex) ? nt.getManName().split(",") : nt.getWomanName().split(",");
        String firstName = firstNames[new Random().nextInt(firstNames.length)].trim();

        StringBuilder name = new StringBuilder(firstName);

        if (nt.getLname() != null && !nt.getLname().isEmpty()) {
            String[] lnames = nt.getLname().split(",");
            name.append("·").append(lnames[new Random().nextInt(lnames.length)].trim());
        }

        if (nt.getAlias() != null && !nt.getAlias().isEmpty()) {
            String[] aliases = nt.getAlias().split(",");
            name.append("·").append(aliases[new Random().nextInt(aliases.length)].trim());
        }

        return name.toString();
    }

    /**
     * Multi-level random walk: at each level pick one ID randomly, follow numeric IDs recursively,
     * return the first non-numeric leaf found. Each top-level ID gets equal probability,
     * then within each branch the same logic applies recursively.
     */
    private String randomWalkChooseChain(String[] ids) {
        if (ids == null || ids.length == 0) return null;
        // Filter to valid numeric IDs
        List<Integer> numericIds = new ArrayList<>();
        for (String idStr : ids) {
            try { int id = Integer.parseInt(idStr.trim()); numericIds.add(id); } catch (NumberFormatException e) {}
        }
        if (numericIds.isEmpty()) return null;
        // Pick one random ID
        int pickedId = numericIds.get(new Random().nextInt(numericIds.size()));
        Choose ch = chooseMap.get(pickedId);
        if (ch == null || ch.getList() == null || ch.getList().isEmpty()) return null;

        // Separate children into numeric IDs and text names
        List<String> childNumericIds = new ArrayList<>();
        List<String> textNames = new ArrayList<>();
        for (String item : ch.getList().split(",")) {
            String t = item.trim();
            if (t.isEmpty()) continue;
            if (t.matches("\\d+")) childNumericIds.add(t);
            else textNames.add(t);
        }
        // If there are text names at this level, pick one randomly
        if (!textNames.isEmpty()) {
            return textNames.get(new Random().nextInt(textNames.size()));
        }
        // Otherwise recurse into numeric children
        if (!childNumericIds.isEmpty()) {
            return randomWalkChooseChain(childNumericIds.toArray(new String[0]));
        }
        return null;
    }

    private static final String[] ALIGNMENTS = {
        "守序善良", "中立善良", "混乱善良",
        "守序中立", "绝对中立", "混乱中立",
        "守序邪恶", "中立邪恶", "混乱邪恶"
    };

    private String calculateCamp(Subrace subrace, StringBuilder log, String indent) {
        String result = "111111111";
        log.append(indent).append("阵营计算: 初始=111111111\n");
        if (subrace != null && subrace.getCamp() != null) {
            log.append(indent).append("  亚种(").append(subrace.getName()).append(") camp=").append(subrace.getCamp()).append("\n");
            result = campAnd(result, subrace.getCamp());
            log.append(indent).append("    → ").append(result).append("\n");
        }
        List<Integer> ones = new ArrayList<>();
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '1') ones.add(i);
        }
        log.append(indent).append("  可用阵营(1-index): ").append(ones.isEmpty() ? "[无]" : ones.stream().map(o -> String.valueOf(o + 1)).toList()).append("\n");
        String selected = ones.isEmpty() ? "绝对中立" : ALIGNMENTS[ones.get(new Random().nextInt(ones.size()))];
        log.append(indent).append("  选定阵营: ").append(selected).append("\n");
        return selected;
    }

    private String calculateCamp(Career career, Subcareer subcareer, Subrace subrace, StringBuilder log, String indent) {
        String result = "111111111";
        log.append(indent).append("阵营计算: 初始=111111111\n");
        if (career != null && career.getCamp() != null) {
            log.append(indent).append("  职业(").append(career.getName()).append(") camp=").append(career.getCamp()).append("\n");
            result = campAnd(result, career.getCamp());
            log.append(indent).append("    → ").append(result).append("\n");
        }
        if (subcareer != null && subcareer.getCamp() != null) {
            log.append(indent).append("  子职(").append(subcareer.getName()).append(") camp=").append(subcareer.getCamp()).append("\n");
            result = campAnd(result, subcareer.getCamp());
            log.append(indent).append("    → ").append(result).append("\n");
        }
        if (subrace != null && subrace.getCamp() != null) {
            log.append(indent).append("  亚种(").append(subrace.getName()).append(") camp=").append(subrace.getCamp()).append("\n");
            result = campAnd(result, subrace.getCamp());
            log.append(indent).append("    → ").append(result).append("\n");
        }
        List<Integer> ones = new ArrayList<>();
        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '1') ones.add(i);
        }
        log.append(indent).append("  可用阵营(1-index): ").append(ones.isEmpty() ? "[无]" : ones.stream().map(o -> String.valueOf(o + 1)).toList()).append("\n");
        String selected = ones.isEmpty() ? "绝对中立" : ALIGNMENTS[ones.get(new Random().nextInt(ones.size()))];
        log.append(indent).append("  选定阵营: ").append(selected).append("\n");
        return selected;
    }

    private String campAnd(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return a;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            sb.append(a.charAt(i) == '1' && b.charAt(i) == '1' ? '1' : '0');
        }
        return sb.toString();
    }

    private String joinAttributes(int[] attrs) {
        return String.join(",", Arrays.stream(attrs).mapToObj(String::valueOf).toArray(String[]::new));
    }

    private int calcSum(int[] attrs) {
        return Arrays.stream(attrs).sum();
    }

    private int getBgMoney(Set<String> content) {
        for (String c : content) {
            Money m = moneyMap.get(c);
            if (m != null) return m.getInit();
        }
        return 0;
    }

    private String getLifeLevelFromContent(Set<String> content) {
        for (String c : content) {
            if (moneyMap.containsKey(c)) return c;
        }
        return "乞食";
    }

    private String getLiveLevel(int money, StringBuilder log) {
        return moneyMap.values().stream()
                .filter(m -> money >= m.getInit())
                .max(Comparator.comparingInt(Money::getInit))
                .map(Money::getName)
                .orElse("乞食");
    }

    // ==================== Statistics ====================

    private GenerationStats calculateStats(List<HeroCard> heroes) {
        GenerationStats stats = new GenerationStats();
        if (heroes == null || heroes.isEmpty()) return stats;

        // --- Alignment stats ---
        Map<String, Map<String, Integer>> align = new LinkedHashMap<>();
        align.put("善良", initAlignRow());
        align.put("中立", initAlignRow());
        align.put("邪恶", initAlignRow());
        // category -> (col -> count)
        Map<String, String> campMap = new HashMap<>();
        campMap.put("守序善良", "善良"); campMap.put("中立善良", "善良"); campMap.put("混乱善良", "善良");
        campMap.put("守序中立", "中立"); campMap.put("绝对中立", "中立"); campMap.put("混乱中立", "中立");
        campMap.put("守序邪恶", "邪恶"); campMap.put("中立邪恶", "邪恶"); campMap.put("混乱邪恶", "邪恶");

        Map<String, String> colMap = new HashMap<>();
        colMap.put("守序善良","守序"); colMap.put("中立善良","中立"); colMap.put("混乱善良","混乱");
        colMap.put("守序中立","守序"); colMap.put("绝对中立","中立"); colMap.put("混乱中立","混乱");
        colMap.put("守序邪恶","守序"); colMap.put("中立邪恶","中立"); colMap.put("混乱邪恶","混乱");

        for (HeroCard h : heroes) {
            String cat = campMap.getOrDefault(h.getCamp(), null);
            String col = colMap.getOrDefault(h.getCamp(), null);
            if (cat != null && col != null) {
                align.get(cat).merge(col, 1, Integer::sum);
                align.get(cat).merge("总和", 1, Integer::sum);
            }
        }
        stats.setAlignmentStats(align);

        // --- Attribute stats ---
        String[] attrNames = {"力量","敏捷","体质","智力","感知","魅力"};
        List<GenerationStats.AttrStats> attrList = new ArrayList<>();
        // Total
        if (!heroes.isEmpty()) {
            int sumMin = Integer.MAX_VALUE, sumMax = Integer.MIN_VALUE;
            double sumAvg = 0;
            for (HeroCard h : heroes) {
                sumMin = Math.min(sumMin, h.getSum());
                sumMax = Math.max(sumMax, h.getSum());
                sumAvg += h.getSum();
            }
            sumAvg /= heroes.size();
            attrList.add(new GenerationStats.AttrStats("总属性", sumAvg, sumMax, sumMin));
        }
        // Per attribute
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            int min = heroes.stream().mapToInt(h -> h.getAttributes().get(idx)).min().orElse(0);
            int max = heroes.stream().mapToInt(h -> h.getAttributes().get(idx)).max().orElse(0);
            double avg = heroes.stream().mapToInt(h -> h.getAttributes().get(idx)).average().orElse(0);
            attrList.add(new GenerationStats.AttrStats(attrNames[i], avg, max, min));
        }
        stats.setAttributeStats(attrList);

        // --- Career stats ---
        Map<String, Integer> careerMap = new LinkedHashMap<>();
        for (HeroCard h : heroes) {
            careerMap.merge(h.getCareer(), 1, Integer::sum);
        }
        stats.setCareerStats(careerMap);

        // --- Race stats ---
        Map<String, Integer> raceMap = new LinkedHashMap<>();
        for (HeroCard h : heroes) {
            raceMap.merge(h.getRace(), 1, Integer::sum);
        }
        stats.setRaceStats(raceMap);

        return stats;
    }

    private Map<String, Integer> initAlignRow() {
        Map<String, Integer> row = new LinkedHashMap<>();
        row.put("守序", 0);
        row.put("中立", 0);
        row.put("混乱", 0);
        row.put("总和", 0);
        return row;
    }

    // ==================== Content Classification ====================

    private void classifyContent(Set<String> content, Hero hero) {
        List<String> skills = new ArrayList<>();
        List<String> tools = new ArrayList<>();
        List<String> equips = new ArrayList<>();
        List<String> languages = new ArrayList<>();
        List<String> feats = new ArrayList<>();
        List<String> saves = new ArrayList<>();
        List<String> spells0 = new ArrayList<>();
        List<String> spells1 = new ArrayList<>();
        List<String> spells2 = new ArrayList<>();
        String prayVal = null;
        String styleVal = null;
        String superspellVal = null;
        String fightVal = null;
        List<String> other = new ArrayList<>();

        for (String item : content) {
            // Skip money/life-level entries (stored in hero.live field separately)
            if (moneyMap.containsKey(item)) continue;
            String table = classifyMap.get(item);
            if (table == null) {
                other.add(item);
                continue;
            }
            switch (table) {
                case "skill": skills.add(item); break;
                case "tool": tools.add(item); break;
                case "equip": equips.add(item); break;
                case "language": languages.add(item); break;
                case "feat": feats.add(item); break;
                case "save": saves.add(item); break;
                case "spell":
                    Integer lvl = spellLevels.getOrDefault(item, -1);
                    if (lvl == 0) spells0.add(item);
                    else if (lvl == 1) spells1.add(item);
                    else if (lvl == 2) spells2.add(item);
                    else other.add(item);
                    break;
                case "pray": prayVal = prayVal == null ? item : prayVal + "," + item; break;
                case "style": styleVal = styleVal == null ? item : styleVal + "," + item; break;
                case "superspell": superspellVal = superspellVal == null ? item : superspellVal + "," + item; break;
                case "fight": fightVal = item; break;
                default: other.add(item); break;
            }
        }

        hero.setSkill(truncate(String.join(",", skills), 100));
        hero.setTool(truncate(String.join(",", tools), 300));
        hero.setEquip(truncate(String.join(",", equips), 300));
        hero.setLanguage(truncate(String.join(",", languages), 300));
        hero.setFeat(truncate(String.join(",", feats), 300));
        hero.setSave(truncate(String.join(",", saves), 50));
        hero.setSpell0(truncate(String.join(",", spells0), 300));
        hero.setSpell1(truncate(String.join(",", spells1), 300));
        hero.setSpell2(truncate(String.join(",", spells2), 300));
        hero.setPray(prayVal);
        hero.setStyle(styleVal);
        hero.setSuperspell(superspellVal);
        hero.setFight(fightVal);
        hero.setOther(truncate(String.join(",", other), 500));
    }

    private String truncate(String str, int maxLen) {
        if (str == null || str.isEmpty()) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen);
    }

    // ==================== Logging & Output ====================

    private void saveLog(int heroId, String logText, boolean success) {
        try {
            Files.createDirectories(Paths.get(logDir));
            String name;
            if (success) {
                name = String.format("hero_%d_success.txt", heroId);
            } else {
                name = String.format("attempt_%d_fail_%d.txt", heroId, System.currentTimeMillis() / 1000);
            }
            Files.writeString(Paths.get(logDir, name), logText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HeroCard toHeroCard(Hero hero) {
        HeroCard card = new HeroCard();
        card.setId(hero.getId());
        card.setName(hero.getName());
        card.setSex(hero.getSex());
        card.setRace(hero.getRace());
        card.setCareer(hero.getCareer());
        card.setAttributes(parseAttributes(hero.getAttribute()));
        card.setSum(hero.getSum());
        card.setCamp(hero.getCamp());
        card.setSkills(splitList(hero.getSkill()));
        card.setTools(splitList(hero.getTool()));
        card.setEquips(splitList(hero.getEquip()));
        card.setLanguages(splitList(hero.getLanguage()));
        card.setFeats(splitList(hero.getFeat()));
        card.setSave(hero.getSave());
        card.setMoney(hero.getMoney());
        card.setSpells0(splitList(hero.getSpell0()));
        card.setSpells1(splitList(hero.getSpell1()));
        card.setSpells2(splitList(hero.getSpell2()));
        card.setPray(hero.getPray());
        card.setStyle(hero.getStyle());
        card.setSuperspell(hero.getSuperspell());
        card.setFight(hero.getFight());
        card.setOther(hero.getOther());
        card.setAge(hero.getAge());
        card.setLive(hero.getLive());
        card.setFavorite(hero.getFavorite());
        return card;
    }

    private List<Integer> parseAttributes(String attrStr) {
        if (attrStr == null) return Arrays.asList(0, 0, 0, 0, 0, 0);
        return Arrays.stream(attrStr.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }

    private List<String> splitList(String str) {
        if (str == null || str.isEmpty()) return new ArrayList<>();
        return Arrays.asList(str.split(","));
    }
}
