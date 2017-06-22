package me.berkow.diffeval

import java.text.NumberFormat
import java.util.*

/**
 * Created by konstantinberkow on 6/22/17.
 */
fun Random.nextFloat(min: Float, max: Float): Float {
    return min + (max - min) * nextFloat()
}

fun NumberFormat.prettyFloatArray(array: FloatArray?): String {
    if (array == null) {
        return "null"
    }

    val iMax = array.size - 1
    if (iMax == -1) {
        return "[]"
    }

    val b = StringBuilder()
    b.append('[')
    var i = 0
    while (true) {
        b.append(format(array[i].toDouble()))
        if (i == iMax)
            return b.append(']').toString()
        b.append(", ")
        i++
    }
}