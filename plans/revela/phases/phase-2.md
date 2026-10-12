---
type: planning
entity: phase
plan: "revela"
phase: 2
status: pending
created: "2026-10-12"
updated: "2026-10-12"
---

# Phase 2: Selectivo + looks + healing

> Part of [revela](../plan.md)

## Objective

Edicion local diferencial: puntos de control selectivos (implementacion propia del concepto U Point), vineta, presets y pincel de healing con inpainting.

## Scope

### Includes

- Puntos de control: colocar punto sobre la foto, gesto vertical para elegir ajuste (brillo/contraste/saturacion) y horizontal para intensidad; zona de efecto por similitud de color + decaimiento con la distancia.
- Computo de mascara selectiva en GPU (shader) y unit test del algoritmo en JVM con bitmaps sinteticos.
- Vineta ajustable.
- 4-6 presets/looks propios (nombres y caracterizacion propios, sin clonar los de Snapseed).
- Healing: pincel para marcar zona a borrar; inpainting con OpenCV Android (modulo photo: Telea/FSR), ABIs limitadas a arm64-v8a.
- Integracion del heal en la pila no destructiva como capa materializada cacheada (seed fija).

### Excludes

- Hechizo/retoque facial, OCR, realidad aumentada.
- Healing sobre zonas enormes (documentar limite practico).
- Sincronizacion o historial infinito.

## Prerequisites

- [ ] Fase 1 completada (pipeline de render y pila de ediciones operativos).

## Deliverables

- [ ] Herramienta Selectiva funcional con N puntos por foto.
- [ ] Herramienta Healing funcional sobre objetos pequenos/medianos.
- [ ] Vineta + presets integrados en la barra de herramientas.
- [ ] OpenCV integrado (AAR o build propio) con peso de APK documentado.

## Acceptance Criteria

- [ ] Un punto selectivo afecta solo a la zona prevista (test unitario de mascara con bitmap sintetico + verificacion visual en movil).
- [ ] Healing elimina un objeto pequeno con resultado aceptable en una foto real; undo restaura la foto previa sin recomputar (capa cacheada).
- [ ] Flujo completo no destructivo: selectivo + healing + global conviven en la misma pila con undo/redo coherente.
- [ ] Unit tests de mascara y de integracion de capas en verde.
- [ ] El usuario valida en movil.

## Dependencies on Other Phases

| Phase | Relationship | Notes |
|-------|-------------|-------|
| 1 | blocks | Reusa pipeline GPU y pila de ediciones |

## Notes

- La mascara selectiva se computa contra la imagen con ajustes previos aplicados (orden de la pila), no siempre contra el original.
- Referencia academica para el enfoque de puntos de control: seleccion local por similitud de color y distancia (literatura publica); nada de codigo de Nik/Google.
