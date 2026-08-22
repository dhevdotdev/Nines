package dev.dhev.nines.appearance

/**
 * The three color anchors that give a named palette its expressive identity.
 *
 * These are inputs to Material's tonal palette generator, not literal role
 * tables. The generator still chooses light/dark tones and paired `on*`
 * colors for the current contrast and mode.
 */
data class ThemePaletteRecipe(
    val anchorArgb: Long,
    val companionArgb: Long,
    val highlightArgb: Long,
)

fun AppColorSource.themePaletteRecipe(): ThemePaletteRecipe = when (this) {
    AppColorSource.NINES -> ThemePaletteRecipe(0xFF6750A4L, 0xFF006A6AL, 0xFF984061L)
    AppColorSource.FOREST -> ThemePaletteRecipe(0xFF386A20L, 0xFF006874L, 0xFF745B00L)
    AppColorSource.OCEAN -> ThemePaletteRecipe(0xFF00658CL, 0xFF6750A4L, 0xFF006E1CL)
    AppColorSource.SUNSET -> ThemePaletteRecipe(0xFF984061L, 0xFF775A00L, 0xFF006874L)
    AppColorSource.CATPPUCCIN -> ThemePaletteRecipe(0xFF9A78B8L, 0xFF5C7EAFL, 0xFFD18B8BL)
    AppColorSource.TOKYO_NIGHT -> ThemePaletteRecipe(0xFF526DA8L, 0xFF6B5778L, 0xFF4C7A70L)
    AppColorSource.DRACULA -> ThemePaletteRecipe(0xFF8E6AAEL, 0xFF4F7C78L, 0xFFB56983L)
    AppColorSource.NORD -> ThemePaletteRecipe(0xFF4E6E8EL, 0xFF6D5B82L, 0xFF806B48L)
    AppColorSource.ONE_DARK -> ThemePaletteRecipe(0xFF3F7899L, 0xFF77638BL, 0xFF96713CL)
    AppColorSource.GRUVBOX -> ThemePaletteRecipe(0xFF9A6B1FL, 0xFF4F7450L, 0xFF9B4F4FL)
    AppColorSource.SOLARIZED -> ThemePaletteRecipe(0xFF3F6875L, 0xFF80602FL, 0xFF6E648CL)
    AppColorSource.MONOKAI -> ThemePaletteRecipe(0xFF9B416BL, 0xFF59734BL, 0xFF88652CL)
    AppColorSource.KANAGAWA -> ThemePaletteRecipe(0xFF536F9FL, 0xFF806044L, 0xFF6B668FL)
    AppColorSource.ROSE_PINE -> ThemePaletteRecipe(0xFF896A9FL, 0xFF5B7775L, 0xFF9B5F6BL)
    AppColorSource.EVERFOREST -> ThemePaletteRecipe(0xFF5F7655L, 0xFF477A78L, 0xFF8A6840L)
    AppColorSource.NIGHTFOX -> ThemePaletteRecipe(0xFF536FA8L, 0xFF716087L, 0xFF5D796BL)
    AppColorSource.WALLPAPER, AppColorSource.CUSTOM ->
        error("${name} does not have a named palette recipe")
}
