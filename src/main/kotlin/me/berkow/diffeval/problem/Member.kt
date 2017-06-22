package me.berkow.diffeval.problem

import java.io.Serializable
import java.util.*

/**
 * Created by konstantinberkow on 5/11/17.
 */
data class Member(private val innerVector: FloatArray) : Serializable {

    operator fun get(position: Int): Float {
        return innerVector[position]
    }

    fun toArray(): FloatArray {
        return innerVector
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val member = other as Member

        return Arrays.equals(innerVector, member.innerVector)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(innerVector)
    }
}
