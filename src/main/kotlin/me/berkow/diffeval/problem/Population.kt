package me.berkow.diffeval.problem

import java.io.Serializable
import java.util.*

/**
 * Created by konstantinberkow on 5/11/17.
 */
data class Population(private val members: Array<Member>) : Serializable {

    operator fun get(position: Int): Member {
        return members[position]
    }

    fun size(): Int {
        return members.size
    }

    fun members() = members.toList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as Population

        return Arrays.equals(members, that.members)
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(members)
    }
}
