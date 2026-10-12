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

- [ ] Detectar version de Android del movil via adb (cuando el usuario conecte) y fijar targetSdk definitivo <!-- added: 2026-10-12 -->
- [ ] Usuario conecta movil: adb install + validacion visual ("listo") <!-- added: 2026-10-12 -->

### In Progress

### Completed

- [x] Descargar cmdline-tools oficiales e instalar en ~/Android/Sdk (sin root) <!-- added: 2026-10-12; completed: 2026-10-12, cmdline-tools 22.0 -->
- [x] Aceptar licencias e instalar platform-tools + platforms + build-tools con sdkmanager <!-- added: 2026-10-12; completed: 2026-10-12, platform-tools 37.0.1 + android-37.0 + build-tools 36.0.0 -->
- [x] Crear esqueleto Gradle Kotlin DSL + Compose (app.revela, minSdk 29) + .gitignore <!-- added: 2026-10-12; completed: 2026-10-12, AGP 9.4.1 + Gradle 9.8.1 + Kotlin 2.4.21 + BOM 2026.09.00, compileSdk 37 -->
- [x] Pantalla basica: abrir imagen + zoom/pan <!-- added: 2026-10-12; completed: 2026-10-12, Photo Picker + transformable -->
- [x] ./gradlew assembleDebug exit 0 <!-- added: 2026-10-12; completed: 2026-10-12, app-debug.apk 29,4 MB -->

### Blocked

- [ ] Instalacion y validacion en movil: bloqueada hasta que el usuario conecte el movil con depuracion USB <!-- added: 2026-10-12, reason: no hay dispositivo conectado (adb devices vacio) -->

## Changelog

### 2026-10-12

- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
