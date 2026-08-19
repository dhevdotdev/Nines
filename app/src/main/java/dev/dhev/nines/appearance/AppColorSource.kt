package dev.dhev.nines.appearance

/** The single source from which Nines derives its app-wide Material color scheme. */
enum class AppColorSource(
    val storageValue: String,
    val seedArgb: Long? = null,
) {
    WALLPAPER("wallpaper"),
    NINES("nines", 0xFF6750A4L),
    FOREST("forest", 0xFF2E7D32L),
    OCEAN("ocean", 0xFF0277BDL),
    SUNSET("sunset", 0xFFC2410CL),
    CATPPUCCIN("catppuccin", 0xFFCBA6F7L),
    TOKYO_NIGHT("tokyo_night", 0xFF7AA2F7L),
    DRACULA("dracula", 0xFFBD93F9L),
    NORD("nord", 0xFF88C0D0L),
    ;

    fun effectiveSource(dynamicColorAvailable: Boolean): AppColorSource =
        if (this == WALLPAPER && !dynamicColorAvailable) NINES else this

    companion object {
        val ninesPalettes = listOf(NINES, FOREST, OCEAN, SUNSET)
        val classicThemes = listOf(CATPPUCCIN, TOKYO_NIGHT, DRACULA, NORD)

        fun fromStorage(value: String?): AppColorSource =
            entries.firstOrNull { it.storageValue == value } ?: WALLPAPER
    }
}
