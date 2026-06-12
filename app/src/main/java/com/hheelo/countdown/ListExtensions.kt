package com.hheelo.countdown

internal fun <T> List<T>.replaced(index: Int, item: T): List<T> {
    return toMutableList().also { it[index] = item }
}

internal fun <T> List<T>.removedAt(index: Int): List<T> {
    return toMutableList().also { it.removeAt(index) }
}

internal fun <T> List<T>.moved(from: Int, to: Int): List<T> {
    return toMutableList().also { it.swap(from, to) }
}

private fun <T> MutableList<T>.swap(from: Int, to: Int) {
    val item = this[from]
    this[from] = this[to]
    this[to] = item
}
