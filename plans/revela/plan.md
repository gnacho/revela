---
type: planning
entity: plan
plan: "revela"
status: active
created: "2026-10-12"
updated: "2026-10-12"
---

# Plan: revela

## Problem / Context

El usuario quiere un editor de fotos movil inspirado en la experiencia de Snapseed (Google/Nik Software): edicion tactil rapida, ajustes globales no destructivos, puntos de control selectivos y healing. Snapseed es software propietario; no se copiara codigo ni assets: reimplementacion limpia (clean-room) de funcionalidad generica, que no esta protegida por copyright. Contexto de la conversacion: se evaluaron REA (ingenieria inversa), Snapseed y GymRun; se decidio construir app Android nativa en vez de PWA.

## Target Outcome

App Android nativa (Kotlin + Jetpack Compose) instalable en el movil del usuario via sideload, con flujo completo: abrir imagen, ajustar global y localmente, recortar/geometria, borrar objetos (healing), y exportar JPEG/PNG con EXIF. Todo local, sin cloud ni cuentas.

## Guiding Decisions & Constraints

- Plataforma: app Android nativa, Kotlin, Jetpack Compose, minSdk 29.
- 100% local y offline: sin cloud, sin cuentas, sin permisos de red.
- Edicion NO destructiva: pila de ediciones reversible; la imagen original nunca se modifica.
- Sin copiar codigo, assets, nombre ni marca de Snapseed/Google. Algoritmos reimplementados desde literatura publica.
- Toolchain Android SIN root: cmdline-tools oficiales en ~/Android/Sdk, sin pacman. JDK de build: Java 17 (ya presente en la maquina).
- Distribucion inicial: APK sideload via adb (debug en fases 0-2, release firmado en fase 3). Repo local sin remoto hasta decision de publicacion.
- Nombre de trabajo: revela; applicationId app.revela (cambiable antes de la release).
- Convenciones git del usuario: conventional commits, sin trazas de IA, sin em dashes.

### Scope-Bounding Assumptions

- Se prueba en el movil fisico del usuario via adb (el usuario conecta el movil y avisa); no se usa emulador.
- ABIs limitadas a arm64-v8a (movil propio; aarch64 es el estandar actual).

## Requirements

### Functional

- [ ] Abrir imagen desde el sistema (Photo Picker) sin permisos de almacenamiento amplios.
- [ ] Ajustes globales en tiempo real: exposicion/brillo, contraste, saturacion, balance de blancos (temperatura + tinte), curvas RGB.
- [ ] Geometria: recorte libre y por ratio, rotar 90 grados, enderezar.
- [ ] Puntos de control selectivos (estilo U Point, implementacion propia): ajuste de brillo/contraste/saturacion en zona definida por similitud de color + distancia.
- [ ] Vineta y presets/looks (4-6).
- [ ] Healing: pincel que elimina objetos pequenos (inpainting).
- [ ] Historial undo/redo completo de la pila de ediciones.
- [ ] Export JPEG/PNG con control de calidad y conservacion de EXIF razonable.

### Non-Functional

- [ ] Previsualizacion fluida (interactiva) sobre imagenes de 12 MP; export a resolucion completa.
- [ ] UI usable con una mano en movil (patron Snapseed de gestos verticales para elegir ajuste y horizontales para el valor; reimplementado a nuestro criterio).

## Scope

### In Scope

- Fases 0-3 de este plan (toolchain, motor, local + healing, cierre MVP).

### Out of Scope

- Cloud, sincronizacion, cuentas, telemetria.
- Edicion de RAW, video, organizador de galeria.
- Play Store / F-Droid (decision futura).
- Wear OS / tablet especifico.

## Definition of Done

- [ ] APK release firmado que instala desde cero en el movil del usuario.
- [ ] Flujo completo verificado en el movil: abrir, ajustar global, punto selectivo, crop, healing, export; la exportacion abre bien y conserva resolucion.
- [ ] Tests unitarios de logica no-UI (pila de ediciones, mascara selectiva, matematica de crop) en verde.
- [ ] README minimo (EN; ES opcional).

## Testing Strategy

- Unit (JVM): pila de ediciones (orden, undo/redo, idempotencia), computo de mascara selectiva, matematica de crop/rotacion.
- Instrumentado minimo (androidTest): render de un ajuste sobre bitmap de prueba y lectura del resultado.
- Manual en movil tras cada fase: gate del usuario (conecta movil, prueba, avisa "listo").

## Phases

| Phase | Title | Contribution | Why Separate | Detail | Status |
|-------|-------|--------------|--------------|--------|--------|
| 0 | Toolchain + esqueleto | Entorno capaz de compilar e instalar la app; base del proyecto | Sin esto no compila nada; gate de dispositivo real | [Phase 0](phases/phase-0.md) | pending |
| 1 | Motor no destructivo + geometria + export | El corazon del editor: ajustes globales en tiempo real con pila reversible y export | Unica unidad coherente de render + estado; sin export no hay app usable | [Phase 1](phases/phase-1.md) | pending |
| 2 | Selectivo + looks + healing | Diferencial de la app: edicion local tipo U Point y borrado de objetos | Depende del motor de la fase 1; es la pieza mas compleja | [Phase 2](phases/phase-2.md) | pending |
| 3 | Cierre MVP | Rendimiento, identidad, release firmada, docs | Pulido separado para no mezclar features con release | [Phase 3](phases/phase-3.md) | pending |

## Risks & Open Questions

| Risk/Question | Impact | Mitigation/Answer |
|---------------|--------|-------------------|
| Rendimiento GPU del pipeline de ajustes en Compose | Preview a tirones | Fase 1: perfilado temprano con imagen 12 MP; fallback RenderEffect/AGSL vs GLSurfaceView segun medidas |
| Tamano de OpenCV Android (~decenas de MB por ABI) | APK gordo | Solo arm64-v8a; valorar modulo photo nativo propio si pesa demasiado |
| Healing no destructivo caro (recomputar inpaint) | Lag al deshacer | El heal se materializa como capa cacheada con seed fija; undo restaura la capa previa |
| Version de Android del movil del usuario | minSdk equivocado | Verificar con adb en fase 0 antes de fijar targetSdk definitivo |
| Estado actual de patentes U Point (Nik) | Riesgo si se publica | Implementacion propia desde literatura; si algun dia se publica, revisar familia de patentes concreto |

## Changelog

### 2026-10-12

- Plan created (4 fases, decision del usuario: Android nativo, nucleo + healing, nombre revela).
