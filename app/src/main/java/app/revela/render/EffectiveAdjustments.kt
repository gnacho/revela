package app.revela.render

import app.revela.core.ColorMath
import app.revela.core.CurvePoint
import app.revela.core.Edit
import app.revela.core.EditStack

/** Plega la pila de edits en ajustes efectivos (la ultima entrada de cada herramienta gana). */
object EffectiveAdjustments {

    fun from(stack: EditStack): ColorMath.Adjustments {
        var a = ColorMath.Adjustments()
        for (edit in stack.edits) {
            a = when (edit) {
                is Edit.Exposure -> a.copy(exposureStops = edit.stops)
                is Edit.Contrast -> a.copy(contrast = edit.amount)
                is Edit.Saturation -> a.copy(saturation = edit.amount)
                is Edit.WhiteBalance -> a.copy(temperature = edit.temperature, tint = edit.tint)
                is Edit.Curves -> a.copy(curve = edit.points)
            }
        }
        return a
    }

    /** Sustituye SOLO el campo de la herramienta activa por el valor en curso (pre-commit). */
    fun withWorking(base: ColorMath.Adjustments, toolId: String, working: ColorMath.Adjustments?): ColorMath.Adjustments {
        if (working == null) return base
        return when (toolId) {
            app.revela.core.Tool.EXPOSURE -> base.copy(exposureStops = working.exposureStops)
            app.revela.core.Tool.CONTRAST -> base.copy(contrast = working.contrast)
            app.revela.core.Tool.SATURATION -> base.copy(saturation = working.saturation)
            app.revela.core.Tool.WHITE_BALANCE -> base.copy(temperature = working.temperature, tint = working.tint)
            app.revela.core.Tool.CURVES -> base.copy(curve = working.curve)
            else -> base
        }
    }

    /** Valor actual del parametro de la herramienta para inicializar el gesto. */
    fun valueOf(a: ColorMath.Adjustments, toolId: String, wbParam: WbParam): Float = when (toolId) {
        app.revela.core.Tool.EXPOSURE -> a.exposureStops
        app.revela.core.Tool.CONTRAST -> a.contrast
        app.revela.core.Tool.SATURATION -> a.saturation
        app.revela.core.Tool.WHITE_BALANCE -> if (wbParam == WbParam.TEMPERATURE) a.temperature else a.tint
        else -> 0f
    }

    /** Construye el edit correspondiente a una herramienta con el valor final del gesto. */
    fun buildEdit(toolId: String, base: ColorMath.Adjustments, wbParam: WbParam, value: Float): Edit? =
        when (toolId) {
            app.revela.core.Tool.EXPOSURE -> Edit.Exposure(value)
            app.revela.core.Tool.CONTRAST -> Edit.Contrast(value)
            app.revela.core.Tool.SATURATION -> Edit.Saturation(value)
            app.revela.core.Tool.WHITE_BALANCE ->
                if (wbParam == WbParam.TEMPERATURE) {
                    Edit.WhiteBalance(temperature = value, tint = base.tint)
                } else {
                    Edit.WhiteBalance(temperature = base.temperature, tint = value)
                }
            else -> null
        }

    fun rangeFor(toolId: String): ClosedFloatingPointRange<Float> = when (toolId) {
        app.revela.core.Tool.EXPOSURE -> -2f..2f
        else -> -1f..1f
    }

    fun formatValue(toolId: String, wbParam: WbParam, value: Float): String = when (toolId) {
        app.revela.core.Tool.EXPOSURE -> {
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
