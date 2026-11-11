package dev.dhev.nines.data

/** Source of demo habits for manual testing. Only debug builds bind a real one; release uses [None]. */
interface DemoData {
    val isAvailable: Boolean

    suspend fun seed()

    object None : DemoData {
        override val isAvailable = false

        override suspend fun seed() = Unit
    }
}
