---
type: planning
entity: todo
plan: "revela"
updated: "2026-10-18"
---

# Todo: revela

> Tracking [revela](plan.md) - alcance completo tras analisis REA ([rea-analysis.md](rea-analysis.md))

## Active Phase: 5 - Producto terminado (REA + bienvenida + estilos)

### Phase Context

- **Scope**: fases 5-12 del backlog completo (abajo)
- **Implementation**: rama local, commits por iteracion, sin push (repo sin remoto)
- **Relevant Docs**: plan.md, rea-analysis.md, phases/

### Pending

- [x] Analisis REA de Snapseed completo (28 tools, medidas, gestos, flujos) <!-- done: 2026-10-18 -->
- [ ] BUG: iconos invisibles en el OnePlus Open (release 0.1.0): en el emulador el MISMO APK los muestra bien (verificado 18-Oct); pide captura de pantalla al usuario o comparte pantalla; hipotesis: OxygenOS 16 + algo especifico del dispositivo, NO R8 <!-- added: 2026-10-18 -->
- [ ] FASE 5 (en curso): panel Herramientas overlay animado slide-up ~250ms + scrim, foto SIEMPRE visible, tab bar persistente (patron Snapseed medido: panel hasta 68% inferior) <!-- added: 2026-10-18 -->
- [ ] FASE 5: renombrados ES de Snapseed: Sanar->Quitamanchas, Ajustar->Ajustar imagen; rejilla 5 columnas <!-- added: 2026-10-18 -->
- [ ] FASE 6: AUTO en Ajustar imagen (auto-niveles histograma + snackbar Deshacer) = "mejorar foto" del usuario; histograma live + comparar antes/despres (mantener pulsado) <!-- added: 2026-10-18 -->
- [ ] FASE 7: ASISTENTE DE BIENVENIDA propio (3 pasos: que es revela / 100% offline-privacidad / gesto vertical-horizontal de Ajustar imagen con demo interactiva); saltarlo al abrir la 2a vez, reanudable desde el menu <!-- added: 2026-10-18 -->
- [ ] FASE 8: ESTILOS personalizables ("Personalizar" del usuario): crear estilo propio desde los ajustes actuales, renombrar, borrar, reordenar; persistidos en la sesion <!-- added: 2026-10-18 -->
- [ ] FASE 9: Ambiance (param Tune tipo Snapseed) + Detalles (estructura+enfoque via unsharp) + Balance de blancos como tool (temp+tinte+auto) <!-- added: 2026-10-18 -->
- [ ] FASE 10: Geometria completa: Girar 90, Perspectiva (libre y vertical/horizontal), Expandir (relleno inteligente simple: clamp/borroso) <!-- added: 2026-10-18 -->
- [ ] FASE 11: Contraste tonal (sombras/medios/luces), HDR-scape suave (2 frames sinteticos), B/N con filtros de color (rojo/amarillo/verde/azul) <!-- added: 2026-10-18 -->
- [ ] FASE 12: Creativas: Texto (tipografias + color + desplazamiento), Marcos (bordes propios sin assets de terceros), Doble exposicion (blend modes), Desenfoque de lente (bokeh radial/lineal) <!-- added: 2026-10-18 -->
- [ ] FASE 13: Pincel (dodge/burn/exposicion/saturacion/temp por pinceladas con mascara), Selectivo v2 (visualizar mascara, umbral de area, mas params) <!-- added: 2026-10-18 -->
- [ ] FASE 14: Stack avanzado: View edits por capas (ocultar/modificar/eliminar por herramienta), Undo/Redo/Revert en sheet, export con Settings (sizing long-edge, formato JPG/PNG, calidad) + Share intent Android <!-- added: 2026-10-18 -->
- [ ] FASE 15: Settings de revela (tema, export) + "Tutoriales" propios (tips por herramienta, patron Snapseed) <!-- added: 2026-10-18 -->
- [ ] Pendiente de fase 3: decision de publicacion (repo publico AGPL + landing) <!-- added: 2026-10-12 -->
- [ ] Pulir radios de agarre del crop (26dp -> ~18dp) y layout del slider Enderezar en pantallas estrechas <!-- added: 2026-10-12 -->
- [ ] Calidad del inpaint en zonas grandes (valorar OpenCV si no basta la difusion) <!-- added: 2026-10-12 -->
- [ ] Validar en el OnePlus fisico: gestos tactiles + rendimiento (cable flaky) <!-- added: 2026-10-12 -->

### In Progress

- [ ] FASE 6: AUTO en Ajustar imagen (auto-niveles histograma + snackbar Deshacer) + histograma live + comparar antes/después <!-- added: 2026-10-18 -->

### Completed

- [x] FASE 5: panel Herramientas overlay animado (slide-up 250ms + scrim, foto SIEMPRE visible), rejilla 3 col, renombrados Ajustar imagen/Quitamanchas; fix crash al abrir URI rota (decode try/catch + estado intacto); gates unlazy 5/5 en emulador release <!-- completed: 2026-10-18 -->
- [x] FASE 0: toolchain + esqueleto + build <!-- completed: 2026-10-12 -->
- [x] FASE 1: nucleo + UI + export + tests + validacion emulador + fixes (bounds-trap, rotacion, recorte v2, pinch, configChanges) <!-- completed: 2026-10-12 -->
- [x] FASE 2: FramePipeline compartido (sanar->color->selectivo->vineta) + EffectiveAdjustments a core <!-- completed: 2026-10-12, 372fd00 -->
- [x] FASE 2: Selectivo (puntos drag/chips/borrado), Vineta, Estilos (7 looks), Sanar (inpaint difusion Kotlin sin OpenCV) <!-- completed: 2026-10-12 -->
- [x] FASE 2: 37/37 tests + validacion en emulador (vineta medida, B/N sat 0.013, punto selectivo persistido con brightness, inpaint visible) <!-- completed: 2026-10-12 -->
- [x] FASE 2 extra: validacion tactil OPPO fisico + pulido crop 18dp <!-- completed: 2026-10-12 -->
- [x] FASE 3: perf medido (decode 168 ms, render 58-64 ms, export 27 MP 577 ms), fixes export, release firmado (R8 ~2 MB) <!-- completed: 2026-10-13 -->
- [x] FASE 4: UI estilo Snapseed (3 tabs, rejilla, TUNE vertical/horizontal, carrusel thumbs vivas, menu revertir), README + capturas, fixes del gesto y crop <!-- completed: 2026-10-18, 3094a28, d8580f9, 51c133d -->

## Changelog

### 2026-10-18
- Analisis REA de Snapseed (rea-analysis.md) + backlog COMPLETO encolado (fases 5-15): asistente de bienvenida, estilos personalizables, AUTO, histograma, todas las herramientas creativas/geometricas/tonales, pincel, stack por capas, settings export.
- Hallazgo: el mismo release APK muestra iconos bien en emulador; el bug de iconos en el OnePlus es especifico del dispositivo (hipotesis, sin verificar).
### 2026-10-12
- Plan creado (4 fases). Decisiones: Android nativo Kotlin+Compose, nucleo + healing, nombre revela, SDK sin root.
