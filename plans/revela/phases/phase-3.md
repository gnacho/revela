---
type: planning
entity: phase
plan: "revela"
phase: 3
status: pending
created: "2026-10-12"
updated: "2026-10-12"
---

# Phase 3: Cierre MVP

> Part of [revela](../plan.md)

## Objective

Producto minimamente terminado: rendimiento pulido, identidad propia, APK release firmado y documentacion minima; decision de publicacion con el usuario.

## Scope

### Includes

- Pasada de rendimiento: apertura de imagenes grandes (24 MP+), memoria, previsualizacion fluida; correcciones que salgan.
- Photo Picker como entrada canonica y flujo compartir-hacia (intent RECEIVE) si el coste es bajo.
- Identidad: nombre visible, icono adaptativo propio, tema visual (Lucide como base de iconografia UI segun preferencia del usuario).
- APK release firmado con keystore propia (keystore fuera del repo, backup documentado en Mi Nube).
- README (EN, ES opcional) con capturas del movil o emulador.
- Decision con el usuario: repo publico (GitHub gnacho) y licencia (propuesta por defecto AGPL-3.0), o se queda local.

### Excludes

- Play Store, F-Droid, firma de distribucion externa.
- Landing page (solo si el usuario la pide; iria con su propia skill).

## Prerequisites

- [ ] Fases 0-2 completadas y validadas en movil.

## Deliverables

- [ ] APK release firmado + keystore respaldada.
- [ ] Icono y nombre propios visibles en el launcher.
- [ ] README con capturas reales.
- [ ] Decision de publicacion tomada con el usuario.

## Acceptance Criteria

- [ ] Flujo completo (abrir, global, selectivo, healing, crop, export) sin cuelgues en el movil del usuario.
- [ ] Instalacion desde cero del APK release exit 0 via adb.
- [ ] README y assets en el repo; keystore NUNCA commiteada.
- [ ] El usuario da el OK final del MVP.

## Dependencies on Other Phases

| Phase | Relationship | Notes |
|-------|-------------|-------|
| 2 | blocks | Es el pulido de lo anterior |

## Notes

- La regla del usuario de releases (confirmacion explicita antes de cortar) aplica aqui: la release final se corta solo con su OK.
- Si se publica: SECURITY.md + private vulnerability reporting (regla vigente en sus repos publicos), sin datos personales en capturas.
