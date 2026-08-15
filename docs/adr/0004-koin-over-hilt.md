# Koin for dependency injection, not Hilt

Nines uses Koin (`org.koin.*`) for DI rather than Hilt/Dagger, wired in `di/AppModule.kt` with `viewModelOf`.

**Why**: no annotation-processor codegen step (faster iteration, simpler build), and DI failures surface as plain runtime exceptions with readable messages rather than generated-code stack traces — a meaningful advantage while learning both Android and the DI graph at once. This matched what newer/single-module Material 3 Expressive apps in the reference survey converged on at this project's scale (single `:app` module, no multi-module boundaries that would benefit from Hilt's compile-time graph validation).

**Consequences**: no compile-time verification that the DI graph is complete — a missing binding fails at runtime, not build time. Revisit if the module count grows enough that Hilt's compile-time graph checking would start paying for itself.
