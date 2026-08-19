# Expressive homepage Add Habit action

**Status:** ready-for-agent

## Problem Statement

The homepage Add Habit control performs the right action, but its large, plus-only treatment reads
as generic chrome beside Nines' tonal, shape-rich Habit cards. It does not communicate creation as
a distinct, expressive moment or match the visual hierarchy of the rest of the screen.

## Solution

Keep the existing Add Habit action pinned at the bottom end of the homepage, but present it as a
compact extended control: a plus icon and the literal label “Add habit” on a Nines Cookie9-sided
surface. It derives its colors from the active Material color scheme, gives a quick local spatial
press response with haptic feedback, and opens the existing Habit editor without transitioning into
it.

## User Stories

1. As a person viewing my Habits, I want a clearly labelled Add habit action, so that I know how to
   start tracking a new Habit without having to infer a plus-only icon.
2. As a person scrolling my Habit list, I want Add habit to remain in a predictable bottom-end
   location, so that creation remains available without returning to the top.
3. As a person using Nines, I want creation to feel visually related to the expressive Habit cards,
   so that the homepage feels like one considered system instead of unrelated controls.
4. As a person using a wallpaper-derived color scheme, I want Add habit to adapt while staying
   legible, so that it remains coherent with the rest of the app.
5. As a person choosing a curated app palette, I want the action to use that palette’s semantic
   colors, so that it does not fall back to a hard-coded brand color.
6. As a person tapping Add habit, I want immediate, local feedback, so that the control feels
   responsive before the editor appears.
7. As a person sensitive to distracting transitions, I want the editor to open normally rather than
   having the action zoom or morph into it, so that creation does not feel visually noisy.
8. As a person using accessibility services, I want the control to retain an unambiguous Add habit
   label, so that its purpose is available without relying on its shape or color.
9. As a person creating a Habit, I want the existing editor and save flow to behave exactly as it
   already does, so that a visual redesign does not change my data or navigation.
10. As a person using the homepage in light and dark themes, I want the control’s icon and label to
    stay readable, so that visual expression does not sacrifice contrast.

## Implementation Decisions

- The homepage creation action remains in the existing scaffold floating-action slot, preserving its
  pinned bottom-end placement and its existing editor-opening callback.
- The visual surface is a medium extended Material floating action button, with a plus icon and
  “Add habit” label. Its idle shape is `Cookie9Sided` from Material Expressive shapes.
- The action explicitly uses `tertiaryContainer` and `onTertiaryContainer` from the active Material
  color scheme. This keeps wallpaper dynamic color and curated palettes as the sole color source.
- A locally scoped, fast spatial scale response is driven from the action’s pressed interaction;
  existing haptic feedback remains at activation. It does not coordinate with the editor sheet and
  therefore creates no zoom, squeeze, shared-element, or hero transition.
- The presentation is extracted as a dedicated homepage composable rather than adding more visual
  behavior to the already large screen composition. No data model, `HabitsUiState`, action, Room,
  navigation, or editor interface changes are needed.
- This follows ADR-0009: geometry feedback uses an expressive spatial spec; color is unchanged by
  the press interaction. Material 3’s FAB guidance was consulted for persistent placement, medium
  size tier, and semantic color variants.

## Testing Decisions

- The highest useful seam is observable homepage behavior on a connected device: placement while
  scrolling, visible label/icon, active-scheme legibility, local press behavior, accessibility
  description, and opening the unchanged editor.
- This redesign adds no pure domain logic and does not need a new unit-test seam or ViewModel/UI
  test infrastructure. Existing tests continue to protect Habit creation and persistence behavior.
- Build/type checking is run during implementation, followed by the full JVM and connected Android
  test suites. The final debug build is installed and launched on the connected phone.

## Out of Scope

- Changing the Habit editor, creation/persistence flow, navigation, or detail-screen numeric-entry
  controls.
- Adding an Add Habit menu, a second creation action, filters, categories, or new theme options.
- A hero animation or transformation between the action and editor sheet.

## Further Notes

- The raw triage brief remains in `idea.md` for history.
- Material guidance consulted: https://m3.material.io/components/floating-action-button/overview
