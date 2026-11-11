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
    ONE_DARK("one_dark", 0xFF61AFEFL),
    GRUVBOX("gruvbox", 0xFFFABD2FL),
    SOLARIZED("solarized", 0xFF268BD2L),
    MONOKAI("monokai", 0xFFF92672L),
    KANAGAWA("kanagawa", 0xFF7E9CD8L),
    ROSE_PINE("rose_pine", 0xFFC4A7E7L),
    EVERFOREST("everforest", 0xFFA7C080L),
    NIGHTFOX("nightfox", 0xFF719CD6L),
    CUSTOM("custom"),
    ;

    fun effectiveSource(dynamicColorAvailable: Boolean): AppColorSource = if (this == WALLPAPER && !dynamicColorAvailable) NINES else this

    companion object {
        val ninesPalettes = listOf(NINES, FOREST, OCEAN, SUNSET)
        val themeCollection =
            listOf(
                CATPPUCCIN,
                TOKYO_NIGHT,
                DRACULA,
                NORD,
                ONE_DARK,
                GRUVBOX,
                SOLARIZED,
                MONOKAI,
                KANAGAWA,
                ROSE_PINE,
                EVERFOREST,
                NIGHTFOX,
            )

        fun fromStorage(value: String?): AppColorSource = entries.firstOrNull { it.storageValue == value } ?: WALLPAPER
    }
}
