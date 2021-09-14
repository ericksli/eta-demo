package net.swiftzer.etademo.common

interface Mapper<T, R> {
    suspend fun map(o: T): R
}
