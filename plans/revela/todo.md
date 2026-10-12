---
type: planning
entity: todo
plan: "revela"
updated: "2026-10-12"
---

# Todo: revela

> Tracking [revela](plan.md)

## Active Phase: 0 - Toolchain + esqueleto

### Phase Context

- **Scope**: [Phase 0](phases/phase-0.md)
- **Implementation**: Not authored yet
- **Latest Handover**: None
- **Relevant Docs**: plan.md (decisiones de stack), memory-casa `proyectos.md` (estructura de repos), `este_equipo.md` (toolchain de la maquina)

### Pending

- [ ] Descargar cmdline-tools oficiales e instalar en ~/Android/Sdk (sin root) <!-- added: 2026-10-12 -->
- [ ] Aceptar licencias e instalar platform-tools + platforms + build-tools con sdkmanager <!-- added: 2026-10-12 -->
- [ ] Detectar version de Android del movil via adb (cuando el usuario conecte) y fijar targetSdk <!-- added: 2026-10-12 -->
- [ ] Crear esqueleto Gradle Kotlin DSL + Compose (app.revela, minSdk 29) + .gitignore <!-- added: 2026-10-12 -->
- [ ] Pantalla basica: abrir imagen + zoom/pan <!-- added: 2026-10-12 -->
- [ ] ./gradlew assembleDebug exit 0 <!-- added: 2026-10-12 -->
- [ ] Usuario conecta movil: adb install + validacion visual ("listo") <!-- added: 2026-10-12 -->

### In Progress

### Completed

### Blocked

## Changelog

### 2026-10-12

- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
