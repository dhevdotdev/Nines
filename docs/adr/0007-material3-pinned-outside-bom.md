# material3 version is pinned directly, overriding the Compose BOM

`gradle/libs.versions.toml` declares `material3 = "1.5.0-alpha25"` and wires `androidx-compose-material3` to that version directly, rather than letting `androidx.compose.bom` (pinned separately, currently `2026.06.01`) resolve it.

**Why**: several Material 3 Expressive APIs this app relies on (e.g. `SwipeToDismissBox`'s `onDismiss` callback, see `docs/adr/0003-custom-swipe-gesture.md`) only exist in alpha releases ahead of what the BOM's stable line currently ships. The BOM is still used for every other Compose artifact — only `material3` is deliberately overridden.

**Consequences**: this is a live version-risk surface — an alpha release can change or remove APIs between versions. When bumping `material3`, re-check any experimental API this app depends on (`MaterialShapes`, `LoadingIndicator`, `SwipeToDismissBox`'s `onDismiss`) against that release's changelog before assuming it still behaves the same way.
