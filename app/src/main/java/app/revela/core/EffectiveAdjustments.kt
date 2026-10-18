package app.revela.core

import app.revela.core.Look

/** Plega la pila de edits en ajustes globales efectivos (la ultima entrada de cada herramienta gana). */
object EffectiveAdjustments {

    fun from(stack: EditStack): ColorMath.Adjustments = fromEdits(stack.edits)

    fun fromEdits(edits: List<Edit>): ColorMath.Adjustments {
        var a = ColorMath.Adjustments()
        for (edit in edits) {
            a = when (edit) {
                is Edit.Exposure -> a.copy(exposureStops = edit.stops)
                is Edit.Contrast -> a.copy(contrast = edit.amount)
                is Edit.Saturation -> a.copy(saturation = edit.amount)
                is Edit.WhiteBalance -> a.copy(temperature = edit.temperature, tint = edit.tint)
                is Edit.Curves -> a.copy(curve = edit.points)
                is Edit.Look -> {
                    val look = Look.byId(edit.id)
                    a.copy(
                        exposureStops = (a.exposureStops + look.dExposure).coerceIn(-2f, 2f),
                        contrast = (a.contrast + look.dContrast).coerceIn(-1f, 1f),
                        saturation = (a.saturation + look.dSaturation).coerceIn(-1f, 1f),
                        temperature = (a.temperature + look.dTemperature).coerceIn(-1f, 1f)
                    )
                }
                // sanar/selectivo/vineta son pases de marco, no ajustes globales
                is Edit.Heal, is Edit.Selective, is Edit.Vignette -> a
            }
        }
        return a
    }

    /** Sustituye SOLO el campo de la herramienta activa por el valor en curso (pre-commit). */
    fun withWorking(base: ColorMath.Adjustments, toolId: String, working: ColorMath.Adjustments?): ColorMath.Adjustments {
        if (working == null) return base
        return when (toolId) {
            Tool.EXPOSURE -> base.copy(exposureStops = working.exposureStops)
            Tool.CONTRAST -> base.copy(contrast = working.contrast)
            Tool.SATURATION -> base.copy(saturation = working.saturation)
            Tool.WHITE_BALANCE -> base.copy(temperature = working.temperature, tint = working.tint)
            Tool.CURVES -> base.copy(curve = working.curve)
            else -> base
        }
    }

    /** Valor actual del parametro de la herramienta para inicializar el gesto. */
    fun valueOf(a: ColorMath.Adjustments, toolId: String, wbParam: WbParam): Float = when (toolId) {
        Tool.EXPOSURE -> a.exposureStops
        Tool.CONTRAST -> a.contrast
        Tool.SATURATION -> a.saturation
        Tool.WHITE_BALANCE -> if (wbParam == WbParam.TEMPERATURE) a.temperature else a.tint
        else -> 0f
    }

    /** Construye el edit correspondiente a una herramienta con el valor final del gesto. */
    fun buildEdit(toolId: String, base: ColorMath.Adjustments, wbParam: WbParam, value: Float): Edit? =
        when (toolId) {
            Tool.EXPOSURE -> Edit.Exposure(value)
            Tool.CONTRAST -> Edit.Contrast(value)
            Tool.SATURATION -> Edit.Saturation(value)
            Tool.WHITE_BALANCE ->
                if (wbParam == WbParam.TEMPERATURE) {
                    Edit.WhiteBalance(temperature = value, tint = base.tint)
                } else {
                    Edit.WhiteBalance(temperature = base.temperature, tint = value)
                }
            else -> null
        }

    fun rangeFor(toolId: String): ClosedFloatingPointRange<Float> = when (toolId) {
        Tool.EXPOSURE -> -2f..2f
        else -> -1f..1f
    }

    fun formatValue(toolId: String, wbParam: WbParam, value: Float): String = when (toolId) {
        Tool.EXPOSURE -> {
            val stops = (value * 10).toInt() / 10f
            (if (stops > 0) "+" else "") + "$stops EV"
        }
        else -> {
            val pct = (value * 100).toInt()
            (if (pct > 0) "+" else "") + "$pct"
        }
    }

    fun defaultCurvePoints(): List<CurvePoint> = listOf(CurvePoint(0f, 0f), CurvePoint(1f, 1f))
}

enum class WbParam { TEMPERATURE, TINT }
