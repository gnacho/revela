# Análisis REA (ingeniería inversa) de Snapseed

created: 2026-10-18
updated: 2026-10-18
método: observación directa de Snapseed 6.x (APKPure, firma Nik Software verificada) en emulador x86_64 API 36, capturas y screenrecord. Reimplementación clean-room: solo patrones UX genéricos, cero código/assets de Google/Nik.

## 1. Inventario completo (medido en 1080x2400, densidad 2.625)

### Home (sin foto)
- Top bar: "OPEN" (texto, izq) | capas (stack) | ⓘ | ⋮ (Settings, Tutorials, Help & feedback)
- Centro: círculo grande con "+" y texto "Tap anywhere to open a photo" (toda la pantalla es clicable)
- Tema claro por defecto; dark theme opcional en Settings
- PRIMER ARRANQUE: sin onboarding, directo al home (verificado tras `pm clear`)

### Picker de fotos
- Usa el Photo Picker del sistema (limited access) como bottom sheet: tabs Photos/Albums, aviso de privacidad, álbumes (Camera, Favorites)
- Orden: más reciente primero

### Editor principal
- Foto encajada (fit) sobre lienzo gris claro con MÁRGENES; nunca recortada
- Top bar: OPEN | capas | ⓘ | ⋮
- Strip de ESTILOS: miniaturas vivas (la foto con cada look aplicado) + nombre debajo; y 1917-2211 px (~112 dp)
- Tab bar: LOOKS | TOOLS | EXPORT, 3 segmentos iguales de 360 px (~48 dp de alto), activa en color
- Look: tap = aplicación EN VIVO en preview; barra X / ✓ para confirmar/descartar; el activo se resalta en azul con borde

### Rejilla de HERRAMIENTAS (TOOLS)
- Panel CLARO que sube desde abajo (slide-up ~250 ms; frame intermedio capturado en y=1360, asentado y=760 de 2400 → panel = 68% inferior)
- LA FOTO SIGUE VISIBLE arriba con velo oscuro (scrim); la tab bar permanece abajo
- 28 herramientas en 5 columnas, icono outline + etiqueta:
  Tune Image, Details, Curves, White Balance, Crop, Rotate, Perspective, Expand, Selective, Brush, Healing, HDR Scape, Glamour Glow, Tonal Contrast, Drama, Vintage, Grainy Film, Retrolux, Grunge, Black & White, Noir, Portrait, Head Pose, Lens Blur, Vignette, Double Exposure, Text, Frames
- ES: Ajustar imagen, Detalles, Curvas, Balance de blancos, Recortar, Girar, Perspectiva, Expandir, Selectivo, Pincel, Quitamanchas, HDR-scape, Brillo glamuroso, Contraste tonal, Drama, Vintage, Película granulada, Retrolux, Grunge, B/N, Noir, Retrato, Pose de cabeza, Desenfoque, Viñeta, Doble exposición, Texto, Marcos

### Tune Image (Ajustar imagen) - la herramienta estrella
- Gesto vertical en la foto: menú flotante central con 7 parámetros (Brightness, Contrast, Saturation, Ambiance, Highlights, Shadows, Warmth), fila activa AZUL, valores a la derecha, flechas de scroll ^ v
- Gesto horizontal: ajusta el valor; regla superior fina con ticks + indicador "Param ±N"
- Abajo: histograma (izq) | comparar antes/después (der, mantener)
- Barra inferior: X | icono sliders | VARITA MÁGICA (AUTO) | ✓
- AUTO: ajusta todos los sliders automáticamente + snackbar "Auto Adjusted" con UNDO
- NO tiene tinte como parámetro; tiene "Ambiance" (nuestra ausencia más notable)

### Healing (Quitamanchas)
- UI mínima: X | undo | redo | ✓; comparar arriba-derecha
- Pintar sobre la zona; difusión del entorno (reconstrucción suave del gradiente, verificada)

### Selective (Selectivo)
- Tap = punto de control (U-Point propio) con flechas ↑↓ encima/debajo
- Barra: X | máscara | ↓ | "10 Dodge & Burn" (valor+nombre del parámetro activo; B/C/S ciclan con gesto horizontal sobre el punto) | ↑ | ojo (ver área afectada) | ✓

### Crop (Recortar)
- Rejilla de tercios + esquinas en L blancas
- Ratios scrollables: Free, Original, Square, DIN, 3:2, 4:3 (más 16:9, 5:4... fuera de pantalla)
- Barra: X | rotar 90º | toggle aspecto | ✓

### Edit stack (capas, icono superior)
- Sheet: Undo, Redo (deshab. si vacío), Revert | View edits, QR look...
- View edits = pantalla dedicada con pila lateral: cada edición como capa con icono (Original abajo); capa activa en azul; por capa: ocultar/modificar/eliminar

### Export (EXPORT)
- Sheet: Share (compartir/abrir en otra app) | Save (copia con ajustes por defecto) | Export (copia con sizing/formato/calidad de Settings) | Export as (copia a carpeta elegida)

### Settings (⋮ > Settings)
- Appearance: Dark theme (toggle, off por defecto)
- Export & sharing: Image sizing ("Do not resize" por defecto, long edge) | Format and quality (JPG 95% por defecto; JPG 100%, PNG)

## 2. Huecos de revela vs Snapseed (gap)

Ya alineado en revela: tabs 3, carrusel de estilos con miniaturas vivas, menú flotante vertical con fila azul + haptics, regla superior + indicador, viñeta/curvas/selectivo/sanar/recortar/ajustar.

Faltantes (prioridad producto):
1. Panel de herramientas NO cubre la foto y sin animación (hoy sustituye al viewport) - CRÍTICO, pedido explícito
2. AUTO en Ajustar imagen ("mejorar foto" del usuario) + snackbar UNDO
3. Histograma y comparar antes/después
4. Renombrado a nombres ES de Snapseed: Sanar→Quitamanchas, Ajustar→Ajustar imagen
5. Ambiance (parámetro de Tune que no tenemos)
6. Detalles (estructura + enfoque)
7. Girar 90º, Perspectiva, Expandir
8. Balance de blancos como herramienta (temp+tinte con auto)
9. Contraste tonal (sombras/medios/luces), HDR-scape suave
10. Pincel, Texto, Marcos, Doble exposición, Desenfoque (lens blur), B/N y presets creativos
11. View edits (capas) y export con sizing/formato/calidad en Settings
12. ASISTENTE DE BIENVENIDA: no existe en Snapseed; el usuario lo pidió - diseño propio de revela

## 3. Decisiones de diseño para revela

- La foto NUNCA se oculta (requisito del usuario; en Snapseed el panel deja ver la foto atenuada: seguimos ese patrón)
- Tema oscuro propio (identidad de revela) con estructura Snapseed
- 5 columnas en rejilla como la referencia; con 7-8 tools caben 2 filas
- AUTO = auto-niveles por histograma (percentiles 0.5/99.5) + microsaturación, aplicado como valores de sliders (no como capa opaca) para mantener el modelo no destructivo de EditStack
