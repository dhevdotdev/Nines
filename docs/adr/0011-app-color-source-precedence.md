# App color source resolves once at the root theme

Nines persists one app color source: Wallpaper or a named curated palette. The
root theme resolves that source to the complete Material light/dark ColorScheme
and every surface consumes that scheme. Wallpaper uses Android's system dynamic
scheme where it is available; otherwise it falls back to Nines.

**Why**: applying a source choice per screen would make previews untruthful,
could leave dialogs/system bars on a different palette, and would make the
existing Habit accent harmonization depend on navigation state. A single root
source keeps personalization coherent while preserving Material semantic color
role pairings and contrast.

**Consequences**: curated palettes are defined as source colors and generated
through HCT/Material color utilities rather than hand-authored role values.
Per-Habit colors remain identity seeds, not alternate app themes, and continue
to harmonize against the root primary. Theme mode stays system-controlled until
a separate decision changes it.
