---
type: planning
entity: todo
plan: "revela"
updated: "2026-10-12"
---

# Todo: revela

> Tracking [revela](plan.md)

## Active Phase: 2 - Selectivo + looks + healing

### Phase Context

- **Scope**: [Phase 2](phases/phase-2.md)
- **Implementation**: Not authored yet
- **Latest Handover**: None
- **Relevant Docs**: plan.md, phases/phase-1.md Notes (pipeline CPU), phases/phase-2.md

### Pending

- [ ] Pulir radios de agarre del crop (26dp -> ~18dp) y layout del slider Enderezar en pantallas estrechas <!-- added: 2026-10-12 -->
- [ ] Calidad del inpaint en zonas grandes (valorar OpenCV si no basta la difusion) <!-- added: 2026-10-12 -->
- [ ] Validar en el OPPO fisico: gestos tactiles + rendimiento (pendiente WiFi adb / cable) <!-- added: 2026-10-12 -->

### In Progress

### Completed

- [x] FASE 0: toolchain + esqueleto + build <!-- completed: 2026-10-12 -->
- [x] FASE 1: nucleo + UI + export + tests + validacion emulador + fixes (bounds-trap, rotacion, recorte v2, pinch, configChanges) <!-- completed: 2026-10-12 -->
- [x] FASE 2: FramePipeline compartido (sanar->color->selectivo->vineta) + EffectiveAdjustments a core <!-- completed: 2026-10-12, 372fd00 -->
- [x] FASE 2: Selectivo (puntos drag/chips/borrado), Vineta, Estilos (7 looks), Sanar (inpaint difusion Kotlin sin OpenCV) <!-- completed: 2026-10-12 -->
- [x] FASE 2: 37/37 tests + validacion en emulador (vineta medida, B/N sat 0.013, punto selectivo persistido con brightness, inpaint visible) <!-- completed: 2026-10-12 -->

### Blocked

- [ ] Instalacion en el OPPO fisico: reconexion USB estable o emparejamiento WiFi adb <!-- added: 2026-10-12, reason: cable flaky -->

## Changelog

### 2026-10-12

- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
