package app.revela

/**
 * Tips de una linea por herramienta, al estilo del primer uso de Snapseed.
 * Vive en codigo (no en recursos) porque el texto es estable y se muestra en un dialogo
 * breve; los idiomas se resuelven por [lang] ("es"/"en") para no duplicar 60 strings.
 */
object Tutorials {

    private val tips: Map<String, Pair<String, String>> = mapOf(
        "tune" to ("Arrastra en vertical para elegir el ajuste y en horizontal para cambiar el valor." to
            "Drag vertically to pick an adjustment, horizontally to change its value."),
        "curves" to ("Cada punto de la curva se arrastra. Dobla la linea para recuperar sombras o luces." to
            "Drag the points. Bend the line to recover shadows or highlights."),
        "tone" to ("Sombras, medios y luces por separado: sube las sombras sin lavar los blancos." to
            "Shadows, midtones and highlights separately: lift the shadows without washing the whites."),
        "vignette" to ("Oscurece las esquinas para centrar la mirada en el sujeto." to
            "Darken the corners to pull the eye to the subject."),
        "heal" to ("Pinta sobre una mancha pequena y se rellena con el entorno. Trazos cortos funcionan mejor." to
            "Paint over a small spot and it is filled from its surroundings. Short strokes work best."),
        "crop" to ("Arrastra fuera de la caja para dibujar un recorte nuevo; dentro, para moverlo." to
            "Drag outside the box to draw a new crop; inside, to move it."),
        "rotate" to ("Gira en pasos de 90 grados. El enderezado fino esta en Recortar." to
            "Rotate in 90 degree steps. Fine straightening lives in Crop."),
        "perspective" to ("Arrastra las esquinas para corregir la fuga de las verticales." to
            "Drag the corners to correct converging verticals."),
        "expand" to ("Tira de un borde para ampliar el lienzo; se rellena con el borde o espejado." to
            "Pull an edge to grow the canvas; it fills with the border or a mirror."),
        "selective" to ("Toca para poner un punto y ajusta solo esa zona. Hasta 8 puntos." to
            "Tap to drop a point and adjust only that area. Up to 8 points."),
        "brush" to ("Pinta el efecto donde quieras. Cada parametro es una capa aparte." to
            "Paint the effect where you want it. Each parameter is its own layer."),
        "headPose" to ("Marca los dos ojos y la boca; la cara se gira sin tocar el resto." to
            "Mark both eyes and the mouth; the face turns without touching the rest."),
        "bnw" to ("Elige el filtro por color de origen: rojo realza pieles, azul cielos." to
            "Pick the filter by source colour: red flatters skin, blue deepens skies."),
        "whiteBalance" to ("Temperatura hacia ambar o azul; el tinte corrige el verde y el magenta." to
            "Temperature towards amber or blue; tint fixes green and magenta casts."),
        "details" to ("Estructura da cuerpo a las texturas; el enfoque es mas agresivo." to
            "Structure adds body to texture; sharpening is more aggressive."),
        "hdr" to ("Comprime el rango: recupera cielo y sombras en la misma foto." to
            "Compresses the range: keep the sky and the shadows in the same shot."),
        "grain" to ("Grano determinista, como el de la pelicula. A 100% se ve sobre todo en sombras." to
            "Deterministic film-like grain. At 100% it shows most in the shadows."),
        "glow" to ("Brillo suave por pantalla: favorece los retratos sin perder detalle." to
            "Soft screen-blend glow: flatters portraits without losing detail."),
        "lensBlur" to ("Desenfoca en circulo alrededor de un centro que puedes mover." to
            "Blurs in a circle around a centre you can move."),
        "doubleExpo" to ("Coloca la segunda foto, ajusta la mezcla y baja la opacidad." to
            "Place the second photo, pick the blend and lower the opacity."),
        "textOverlay" to ("Texto sobre la foto con tamano y posicion. Prueba a bajarlo al 40%." to
            "Text over the photo with size and position. Try dropping it to 40%."),
        "frame" to ("Un marco fino separa la foto del fondo de la pantalla." to
            "A thin frame separates the photo from the screen background."),
        "tonalContrast" to ("Sombras, medios y luces por separado: sube las sombras sin lavar los blancos." to
            "Shadows, midtones and highlights separately: lift the shadows without washing the whites."),
        "drama" to ("Contraste dramatico con cuerpo en los medios. A media intensidad no empasta." to
            "Dramatic contrast with body in the midtones. Half strength keeps it from mushing."),
        "vintage" to ("Calidez y negros levantados, como una copia antigua." to
            "Warmth and lifted blacks, like an old print."),
        "grainyFilm" to ("Alto contraste en blanco y negro con grano marcado." to
            "High-contrast black and white with heavy grain."),
        "retrolux" to ("Tonos cruzados y luces suaves: ojo con las pieles." to
            "Cross-processed tones and soft highlights: watch the skin."),
        "grunge" to ("Textura sucia y desaturada para escenas duras." to
            "Dirty, desaturated texture for harsh scenes."),
        "noir" to ("Blanco y negro con negros profundos y vineta." to
            "Black and white with deep blacks and a vignette."),
        "portrait" to ("Piel suave y calida, pensado para retratos en exterior." to
            "Soft, warm skin, made for outdoor portraits."),
        "looks" to ("Los estilos son ajustes completos: apilalos o crea el tuyo con +." to
            "Looks are complete adjustments: stack them or create your own with +.")
    )

    fun tipFor(toolId: String, lang: String): String? {
        val entry = tips[toolId.substringBefore(':')] ?: return null
        return if (lang.startsWith("es", ignoreCase = true)) entry.first else entry.second
    }
}
