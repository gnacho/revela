---
type: planning
entity: todo
plan: "revela"
updated: "2026-10-12"
---

# Todo: revela

> Tracking [revela](plan.md)

## Active Phase: 1 - Motor no destructivo + geometria + export

### Phase Context

- **Scope**: [Phase 1](phases/phase-1.md)
- **Implementation**: Not authored yet
- **Latest Handover**: None
- **Relevant Docs**: plan.md (decisiones), phases/phase-1.md Notes (decision pipeline CPU)

### Pending

- [ ] Validar en el OPPO fisico: instalacion + gestos tactiles + rendimiento (el cable es flaky; pendiente WiFi adb) <!-- added: 2026-10-12 -->
- [ ] Pulir radios de agarre del recorte (26dp -> ~18dp) y layout del slider Enderezar en pantallas estrechas <!-- added: 2026-10-12 -->

### In Progress

### Completed

- [x] FASE 0: toolchain + esqueleto + build <!-- completed: 2026-10-12 -->
- [x] FASE 1: nucleo + UI + export + 24/24 tests <!-- completed: 2026-10-12, b81af78 -->
- [x] FASE 1: banco de pruebas emulador headless (KVM, API 36) + imagen sintetica de test <!-- completed: 2026-10-12 -->
- [x] FASE 1: validacion completa en emulador (picker, exposicion +0.7EV medido, curvas UI, recorte Dibujar con log de rect, rotacion sin deformar, export 734x776 verificado en Imagenes/revela) <!-- completed: 2026-10-12, 683957a -->
- [x] Fixes: export bounds-trap, deformacion por rotacion, recorte v2 (80% + modos Ajustar/Dibujar), pinch zoom, configChanges plegable <!-- completed: 2026-10-12 -->

### Blocked

- [ ] Instalacion en el OPPO fisico: bloqueada hasta reconexion USB estable o emparejamiento WiFi adb <!-- added: 2026-10-12, reason: cable flaky -->

## Changelog

### 2026-10-12

- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
