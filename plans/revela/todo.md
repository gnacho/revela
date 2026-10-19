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

- [ ] Capturas del README desactualizadas (muestran la UI previa al rediseño Snapseed; rehacer con la rejilla 3x3 + wizard) <!-- added: 2026-10-18 -->
- [x] Analisis REA de Snapseed completo (28 tools, medidas, gestos, flujos) <!-- done: 2026-10-18 -->
- [x] BUG iconos OnePlus: RESUELTO - la build nueva los renderiza bien en el dispositivo (captura 18-Oct); era la build vieja <!-- done: 2026-10-18 -->
- [x] FASE 9 parcial HECHA (ver abajo fase 10): Detalles, HDR-scape, Balance de blancos; quedan Perspectiva, Expandir, Ambiance <!-- edited: 2026-10-18 -->
- [x] FASE 11 (desbloqueada por analisis REA del APK real): HOME con biblioteca de fotos abiertas (grid de sesiones persistidas con miniatura, tap reabre, borrado con papelera), barra de favoritos personalizable siempre visible bajo la barra superior (estrella en la rejilla, JSON persistido), cerrar 28/28 (Perspectiva 8 puntos 4 modos free/scale/tilt/rotate, Expandir con relleno borde/espejo, Pincel multi-parametro por trazos, Pose con warp facial por 3 puntos manuales + yaw/pitch), Doble exposicion completa (8 blends incluidos add/overlay/subtract + opacidad + escala/rotacion/offset de la 2a capa con arrastre), animacion look_thumbnail_state en el carrusel <!-- done: 2026-10-18 -->
- [x] FASE 12 HECHA (19-Oct, `d6cf8e7`): editor de capas (ocultar/mostrar sin perder valores, reabrir herramienta, eliminar capa), Undo/Redo/Revertir dentro de la hoja, capas ocultas persistidas en `revela_ocultas.json`; gates G26 (la imagen cambia de verdad al ocultar) y G27 (persiste tras force-stop) <!-- done: 2026-10-19 -->
- [ ] FASE 12 (resto ya cubierto antes): Stack avanzado: View edits por capas (ocultar/modificar/eliminar por herramienta), Undo/Redo/Revert en sheet, export con Settings (sizing long-edge, formato JPG/PNG, calidad) + Share intent Android <!-- added: 2026-10-18 -->
- [x] FASE 13 HECHA (19-Oct, `64a2d27`): tema claro/oscuro en Configuracion (persistente, G28), consejos por herramienta en la barra superior (27 tips ES/EN, G28) y gestion de estilos personalizados con pulsacion larga (renombrar + mover; G29). Settings de export y Share ya estaban en 0.1.2 <!-- done: 2026-10-19 -->
- [ ] FASE 13 (resto): Settings de revela (tema, export) + "Tutoriales" propios (tips por herramienta, patron Snapseed) + reordenar/renombrar estilos personalizados <!-- added: 2026-10-18 -->
- [x] Publicacion HECHA: github.com/gnacho/revela, publico, AGPL-3.0 (LICENSE + README actualizado con capturas nuevas); landing pendiente <!-- done: 2026-10-18 -->
- [ ] Pulir radios de agarre del crop (26dp -> ~18dp) y layout del slider Enderezar en pantallas estrechas <!-- added: 2026-10-12 -->
- [ ] Calidad del inpaint en zonas grandes (valorar OpenCV si no basta la difusion) <!-- added: 2026-10-12 -->
- [ ] Validar en el OnePlus fisico: gestos tactiles de las 9 tools + wizard + estilos (cable flaky) <!-- added: 2026-10-12 -->

### In Progress

(nada - siguiente: fase 9)

### Completed

- [x] FASE 10 ANTICIPADA (subagente implementador, verificado por orquestador): +14 herramientas -> rejilla de 24 en orden Snapseed con scroll (Detalles boxBlur+unsharp, HDR-scape contraste local, Grano determinista, Brillo glamuroso screen-blend, Desenfoque radial, presets Drama/Vintage/Granulada/Retrolux/Grunge/Noir/Retrato como combos con slider, Doble exposicion 5 blends, Texto y Marcos via TextCompositor Canvas); preview en vivo generico workingEdit; 64/64 tests; emulador release: 24/24 entradas en rejilla con scroll + panel Detalles funcional (Estructura/Enfoque/Aplicar); instalado en OnePlus <!-- completed: 2026-10-18, 101d155 -->
- [x] REFACTOR LAYOUT SNAPSEED (18da2fc): causa raiz de "se deja de ver la foto" = paneles hermanos con weight que ENCOGIAN el viewport; ahora paneles/carrusel/grid son OVERLAYS dentro del viewport de altura fija; verificado con luminancia de franja de foto identica en 3 estados (64.34/64.07/64.34); +Balance de blancos (2 sliders) <!-- completed: 2026-10-18 -->

- [x] FASES 7-8 (lote gap-closure, gates 12/12): Girar (90 izq/der sobre quarterTurns), Blanco y negro con filtros (Neutro/Rojo/Amarillo/Verde/Azul; stage pipeline applyBnw tras color), Contraste tonal (sombras/medios/luces por LUT de zonas coseno +-60 niveles), ESTILOS PERSONALIZABLES (tile + en Estilos: guardar ajustes actuales con nombre, miniatura viva propia via ThumbRender.forEdits, aplicar con tap, borrar con x, persistidos en revela_estilos.json), ASISTENTE DE BIENVENIDA (3 pasos con transiciones slide+fade, Saltar, marca persistente revela_bienvenida.txt, aparece en la 1a apertura - verificado en emulador con instalacion limpia); rejilla 3x3 = 9 herramientas; G8-G10 tests JVM nuevos, G12 verifica wizard+rejilla en release; 52/52 tests. Instalado en OnePlus <!-- completed: 2026-10-18 -->
- [x] FASE 6: boton AUTO de un toque (AutoLevels: percentiles 0.5/99.5 -> exposicion max 0.6EV + contraste estirado + microsaturacion), snackbar "Auto ajustado" con Deshacer (reset al snapshot previo), histograma de luminancia en vivo (48 tramos) en el panel de Ajustar imagen; gates 7/7 (el G7 verifica 0.0 EV -> +0.6 EV en el indicador tras pulsar la varita en release); 41/41 tests. Instalado en el OnePlus: iconos OK en el dispositivo (captura) <!-- completed: 2026-10-18 -->
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
