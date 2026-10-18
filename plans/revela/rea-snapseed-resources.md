# Análisis REA (morluto/rea) del Snapseed real - recursos extraídos

created: 2026-10-18 (noche)
método: REA 6.7.0 (morluto/rea, MIT) + JAR jadx-headless-mcp 0.7.1 verificado por SHA-256 + aapt2 dump resources sobre el APK REAL de Snapseed instalado en el emulador (com.niksoftware.snapseed, 27.8 MB).

## Hallazgo clave sobre el código
El release de Snapseed está OFUSCADO con ProGuard: las clases no tienen nombres legibles (`search-android-classes Tool` de REA devuelve 3 matches sin relación). El análisis de CÓDIGO vía REA da poco; los RECURSOS no se ofuscan y ahí está el dato duro.

## Catálogo de herramientas: 28 EXACTAS (verificado en recursos)
Los 28 títulos existen textualmente en los recursos del APK (busqueda case-insensitive de cada uno):
Tune image, Details, Curves, White balance, Crop, Rotate, Perspective, Expand, Selective, Brush, Healing, HDR Scape, Glamour Glow, Tonal Contrast, Drama, Vintage, Grainy Film, Retrolux, Grunge, Black & white, Noir, Portrait, Head Pose, Lens Blur, Vignette, Double Exposure, Text, Frames.

Las "50" que ve el usuario = 28 herramientas + 11 looks del carrusel + parámetros internos + opciones de export. El contador de revela va por 24/28.

## Faltan en revela (4): Perspective, Expand, Brush, Head Pose
Otros recursos confirmados: perspective tiene 8 control points con modos free/scale/tilt/rotate (a11y strings); Double Exposure tiene 6 blend modes (add, darken, default, lighten, overlay, subtract) + opacity + rotación/resize de la 2ª capa (nosotros tenemos 5 blends sin add/overlay y sin transformaciones de capa); Brush es multi-parámetro con puntos colocables.

## Animaciones
- `animator/look_thumbnail_state`: animador de estado de la miniatura de looks (a replicar: estado seleccionado con transición).
- El resto de animators del APK son de librerías (abc_*, fragment_*). La animación del grid de herramientas está en CÓDIGO ofuscado -> replicar empíricamente: slide-up ~250 ms con interpolador tipo FastOutSlowIn (ya hecho) + verificar en el dispositivo.

## Tareas que desbloquea este análisis (para implementar)
1. HOME con biblioteca de fotos abiertas (grid de sesiones persistidas; tap = reabrir sesión). Hoy revela solo abre una foto.
2. Barra de favoritos persistente y personalizable (siempre visible bajo la barra superior; el usuario elige qué herramientas van).
3. Cerrar catálogo 28/28: Perspective (warp 4 esquinas + modos free/scale/tilt/rotate), Expand, Brush (pinceles multi-param), Head Pose.
4. Doble exposición completa: +blend add/overlay, opacity, rotar/escalar la 2ª capa.
5. Replicar look_thumbnail_state en el carrusel de Estilos.
6. Tamaños de iconos/celdas: los dimens internos de Snapseed no aparecen con nombres claros en el dump (verificación pendiente con apktool completo); ajustar al ojo contra capturas del Snapseed real en el emulador.

## Cómo repetir el análisis
REA_JADX_MCP_JAR=~/.local/share/rea/android/jadx-headless-mcp-0.7.1-all.jar (SHA-256 verificado, 6e5eacf5...)
rea inspect-android-package /tmp/opencode/rea/snapseed.apk --json
~/Android/Sdk/build-tools/36.0.0/aapt2 dump resources /tmp/opencode/rea/snapseed.apk > res.txt
(approximo recursos con aapt2 porque REA solo expone manifest+clases; el código está ofuscado)
