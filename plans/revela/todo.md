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

- [ ] Validar fase 0+1 en movil: adb install + flujo completo (ajustes, curvas, recorte, export) <!-- added: 2026-10-12 -->
- [ ] Detectar version de Android del movil via adb y fijar targetSdk definitivo <!-- added: 2026-10-12 -->

### In Progress

### Completed

- [x] FASE 0: Descargar cmdline-tools oficiales e instalar en ~/Android/Sdk (sin root) <!-- completed: 2026-10-12, cmdline-tools 22.0 -->
- [x] FASE 0: Aceptar licencias e instalar platform-tools + platforms + build-tools <!-- completed: 2026-10-12, platform-tools 37.0.1 + android-37.0 + build-tools 36.0.0 -->
- [x] FASE 0: Esqueleto Gradle Kotlin DSL + Compose (app.revela, minSdk 29) + .gitignore <!-- completed: 2026-10-12, AGP 9.4.1 + Gradle 9.8.1 + Kotlin 2.4.21, compileSdk 37 -->
- [x] FASE 0: Pantalla basica con Photo Picker y zoom/pan <!-- completed: 2026-10-12 -->
- [x] FASE 0: ./gradlew assembleDebug exit 0 <!-- completed: 2026-10-12 -->
- [x] FASE 1: Nucleo puro Kotlin (EditStack undo/redo, ColorMath LUTs, CurveMath monotona, GeometryModel) + 24/24 tests JVM <!-- completed: 2026-10-12, b81af78 -->
- [x] FASE 1: UI editor completa (rail de herramientas, gestos horizontales, editor de curvas interactivo, recorte con tiradores + ratios + enderezado, comparar original, export con dialogo) <!-- completed: 2026-10-12 -->
- [x] FASE 1: Export resolucion completa via MediaStore (Imagenes/revela) con EXIF copiado y orientacion horneada <!-- completed: 2026-10-12 -->
- [x] FASE 1: Persistencia de sesion por imagen (JSON en filesDir) <!-- completed: 2026-10-12 -->
- [x] FASE 1: assembleDebug + testDebugUnitTest exit 0 (APK 62 MB debug sin minificar) <!-- completed: 2026-10-12 -->

### Blocked

- [ ] Instalacion y validacion en movil (fases 0 y 1): bloqueada hasta que el usuario conecte el movil con depuracion USB <!-- added: 2026-10-12, reason: adb devices vacio -->

## Changelog

### 2026-10-12

- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
