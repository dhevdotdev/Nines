package dev.dhev.nines.debug

import dev.dhev.nines.NinesApplication
import dev.dhev.nines.data.DemoData
import org.koin.core.module.Module
import org.koin.dsl.module

class DebugNinesApplication : NinesApplication() {
    override fun variantModules(): List<Module> = listOf(
        module {
            single<DemoData> { DemoDataSeeder(get()) }
        },
    )
}
