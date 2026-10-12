---
type: planning
entity: phase
plan: "revela"
phase: 1
status: pending
created: "2026-10-12"
updated: "2026-10-12"
---

# Phase 1: Motor no destructivo + geometria + export

> Part of [revela](../plan.md)

## Objective

Editor global real y usable: ajustes aplicados en tiempo real sobre GPU con pila de ediciones reversible, herramientas de geometria y exportacion a fichero.

## Scope

### Includes

- Pipeline de render GPU para previsualizacion (candidatos: RenderEffect/AGSL con Compose, o GLSurfaceView; decidir con medidas de rendimiento sobre 12 MP).
- Ajustes globales: exposicion/brillo, contraste, saturacion, balance de blancos (temperatura + tinte), curvas RGB (con puntos arrastrables).
- Modelo de edicion no destructivo: pila de operaciones serializable + undo/redo completo.
- Geometria: crop libre y por ratios (1:1, 4:3, 16:9, original), rotar 90 grados en ambos sentidos, enderezar.
- Export JPEG/PNG con calidad configurable y EXIF razonable, a resolucion completa.
- Interaccion estilo Snapseed: seleccion de herramienta + gesto horizontal para valor (reimplementado a nuestro criterio).

### Excludes

- Puntos de control selectivos, vineta, presets, healing (fase 2).
- RAW, filtros avanzados, texto/etiquetas.
- Organizacion multi-imagen.

## Prerequisites

- [ ] Fase 0 completada (app instalable, movil verificado).

## Deliverables

- [ ] Pantalla de editor con barra de herramientas (Luz, Color, Curvas, Recortar, Rotar) y previsualizacion.
- [ ] Pila de ediciones con undo/redo operativa y testeada (unit).
- [ ] Export a JPEG/PNG verificado (resolucion y EXIF comprobados al reabrir el fichero).

## Acceptance Criteria

- [ ] Ajustes globales responden en tiempo real sobre una foto de 12 MP en el movil (sin preview congelada).
- [ ] Undo/redo recorre toda la pila sin estados corruptos; cerrar y reabrir la sesion conserva la pila (persistencia local de la edicion).
- [ ] Crop/rotar/enderezar con vista previa correcta y export en resolucion completa.
- [ ] Unit tests de pila de ediciones en verde (./gradlew testDebugUnitTest exit 0).
- [ ] El usuario valida el flujo en el movil.

## Dependencies on Other Phases

| Phase | Relationship | Notes |
|-------|-------------|-------|
| 0 | blocks | Necesita SDK + esqueleto compilando |

## Notes

- La pila de ediciones vive en memoria + persistencia local ligera (JSON en filesDir); sin base de datos en esta fase.
- El render final de export reutiliza el mismo pipeline de la preview (una sola implementacion de ajustes).
