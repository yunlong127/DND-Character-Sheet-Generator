package com.dndhero.controller;

import com.dndhero.model.dto.*;
import com.dndhero.service.CharacterGenerationService;
import com.dndhero.service.ExcelExportService;
import com.dndhero.service.WizardService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CharacterController {

    private final CharacterGenerationService service;
    private final WizardService wizardService;
    private final ExcelExportService exportService;

    public CharacterController(CharacterGenerationService service, WizardService wizardService, ExcelExportService exportService) {
        this.service = service;
        this.wizardService = wizardService;
        this.exportService = exportService;
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@RequestBody GenerateRequest request) {
        return service.generate(request);
    }

    @GetMapping("/heroes")
    public List<HeroCard> listHeroes(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "true") boolean asc,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean favoriteOnly) {
        return service.listHeroes(sort, asc, search, favoriteOnly);
    }

    @PostMapping("/heroes/{id}/favorite")
    public HeroCard toggleFavorite(@PathVariable Integer id) {
        return service.toggleFavorite(id);
    }

    @GetMapping("/heroes/stats")
    public Map<String, Object> heroStats(
            @RequestParam(required = false) String search) {
        return service.getHeroStats(search);
    }

    @PostMapping("/wizard/start")
    public WizardStepResponse startWizard(@RequestBody GenerateRequest request) {
        int rollCount = request.getRollCount() != null && request.getRollCount() > 0 ? request.getRollCount() : 3;
        return wizardService.startWizard(request.getFormula(), request.getBackground(), rollCount, request.getMode());
    }

    @PostMapping("/wizard/choose")
    public WizardStepResponse wizardChoose(@RequestBody WizardChoiceRequest req) {
        return wizardService.userChoice(req.getSessionId(), req.getSelected());
    }

    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    @GetMapping("/heroes/{id}/export")
    public ResponseEntity<byte[]> exportHero(@PathVariable Integer id) {
        byte[] data = exportService.exportHero(id);
        if (data == null) return ResponseEntity.notFound().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("hero_" + id + ".xlsx").build());
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }
}
