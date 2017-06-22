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

fun Array<String>.toMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()

    for (i in 0..size - 1 step 2) {
        map.put(get(i), get(i + 1))
    }

    return map.toMap()
}

fun Map<String, String>.getAsFloat(key: String, default: Float = 0F) = get(key)?.toFloat() ?: default

fun Map<String, String>.getAsInt(key: String, default: Int = 0) = get(key)?.toInt() ?: default

fun Map<String, String>.getAsLong(key: String, default: Long = 0) = get(key)?.toLong() ?: default

inline fun <R> doAndMeasure(func: () -> R, consumer: (R, Long) -> Unit) {
    val start = System.nanoTime()
    val result = func()
    val elapsed = System.nanoTime() - start

    consumer(result, elapsed)
}

inline fun <R> doAndMeasure(func: () -> R): Pair<R, Long> {
    val start = System.nanoTime()
    val result = func()
    val elapsed = System.nanoTime() - start

    return Pair(result, elapsed)
}