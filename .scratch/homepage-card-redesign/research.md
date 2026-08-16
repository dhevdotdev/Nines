# Homepage card redesign research

## Findings

- Material 3 Expressive's `ButtonGroup` is an interaction layout, not merely a row of adjacent buttons: pressed children can expand while neighboring children compress, with a default expanded ratio of 15%. It also supports weighted items, custom items, and overflow behavior. [AndroidX ButtonGroup source](https://android.googlesource.com/platform/frameworks/support/+/618d778ba8585cf49f8b2beffe7470ac65850435/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ButtonGroup.kt)
- Google's Androidify sample treats expressive design as a combination of component choice, themed motion, and shape language. It uses `MotionScheme.expressive()`, shape morphing, and press scale for prominent actions rather than making every surface loud. [Androidify: Building delightful UIs with Compose](https://android-developers.googleblog.com/2025/05/androidify-building-delightful-ui-with-compose.html)
- The Material You app list distinguishes `MD3E` from ordinary Material You: Material 3 plus dynamic color plus expressive treatment. Its habit-tracker category includes Respawn, Soberity, Habit Builder, Table Habit, TimePlanner, Routine Tracker, Koduko, Habo, and Terra; these are references for hierarchy and visual rhythm only. [Material You app list](https://github.com/nyas1/Material-You-app-list)
- A polished habit-tracker reference such as Grit uses the detail surface to make progress, consistency, and streaks legible as a visual system rather than relying on a tiny history strip. [Grit screenshot](https://f-droid.org/repo/com.shub39.grit/en-US/phoneScreenshots/4.png)

## Implication for Nines

The homepage card should have one expressive action cluster with a clear primary logging action, while exact numeric editing is a related secondary action. The card needs a second meaningful visual rhythm after removing the dots, but that rhythm should come from selected-date status and target progress, not duplicated recent history.
