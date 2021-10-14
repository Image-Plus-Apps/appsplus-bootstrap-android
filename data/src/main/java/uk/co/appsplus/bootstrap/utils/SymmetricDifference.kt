package uk.co.appsplus.bootstrap.utils

fun <T> Set<T>.symmetricDifference(s: Set<T>): Set<T> {
    return (this subtract s) union (s subtract this)
}
