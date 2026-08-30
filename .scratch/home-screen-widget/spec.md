Status: ready-for-agent

# Home-screen widget

## Problem Statement

Nines currently requires the user to open the app before they can see or act
on today's Habits. A home-screen widget should make the daily habit loop
available at a glance and reduce the friction of simple logging.

The widget must feel like Nines, not like a separate mini-app. In particular,
measurable Entry editing already has an established editor and should not be
rebuilt for the widget.

## Solution

Add a today-focused, vertically scrollable home-screen widget showing the
user's active Habits and their current status.

Boolean Habits can be marked complete directly from the widget. Tapping a
measurable Habit opens a compact, draggable Nines surface over the launcher.
That surface reuses the existing measurable Entry editor. Dragging its handle
up expands into the normal full Nines app.

The widget is an app entry point, not a system overlay. It does not require
display-over-other-apps permission, and it does not introduce a second editor,
scoring model, or persistence path.

## User Stories

1. As a Nines user, I want to see today's active Habits on my home screen, so
   that I can understand what remains without opening Nines.
2. As a Nines user, I want the widget to scroll vertically, so that all my
   Habits remain reachable when they do not fit in the available height.
3. As a Nines user, I want to mark a Boolean Habit complete from the widget,
   so that simple logging does not require opening the app.
4. As a Nines user, I want the widget to show the updated Boolean status after
   I act, so that the widget confirms the change.
5. As a Nines user, I want tapping a measurable Habit to open its value editor,
   so that measurable logging remains possible from the widget.
6. As a Nines user, I want that editor to be the same editor used in Nines,
   so that its Habit identity, date, unit, Target, validation, Save, and Clear
   behavior remain consistent.
7. As a Nines user, I want the editor surface to begin as a compact draggable
   sheet over the launcher, so that the widget interaction feels immediate.
8. As a Nines user, I want to drag the sheet upward into the full app, so that
   I can move from a quick action into the complete Nines experience.
9. As a Nines user, I want to dismiss the quick surface and return to the
   launcher, so that using the widget does not trap me in Nines.
10. As a Nines user, I want the widget and quick surface to use Nines' existing
    visual language, so that the experience feels integrated.

## Implementation Decisions

- The widget is anchored to the device's local today. Historical date
  navigation remains an in-app responsibility.
- The widget shows active Habits and excludes archived Habits.
- The widget is a vertically scrollable collection surface.
- Boolean completion dispatches the existing Entry action for today.
- Measurable Habits launch the existing measurable Entry editor through a
  compact Nines activity/sheet. The editor is not duplicated for the widget.
- The compact surface can expand into the normal full-app experience and can
  be dismissed back to the launcher.
- Widget presentation consumes an existing today-scoped projection. It must
  not independently derive Score, Streak, Gap, frequency progress, or Target
  meaning.
- Empty and all-done states should be intentional, but their exact visual
  treatment is left to implementation within the existing Nines design system.
- The widget must not request `SYSTEM_ALERT_WINDOW` or display-over-other-apps
  permission.

## Testing Decisions

- Test the observable widget projection and actions, not widget implementation
  details.
- Cover active/archive filtering, today's status, Boolean completion, and
  measurable-editor routing at the highest existing application seam.
- Verify repository persistence uses the same Entry semantics as the app.
- Use connected-device smoke testing for adding the widget, vertical scrolling,
  binary completion, measurable editing, sheet expansion, and dismissal.
- Verify the widget updates after changes made both in the widget surface and
  in the full app.

## Out of Scope

- Historical date selection from the widget.
- A widget-specific measurable editor.
- Multiple independent widget configurations.
- Categories, filters, pinned-habit configuration, or advanced customization.
- Export/import and billing.
- Wear OS, lock-screen widgets, Android Auto, or other surfaces.

## Further Notes

Android supports scrollable collection widgets and direct item actions. The
Todoist-like quick surface is therefore modeled as a Nines activity presented
as a bottom-sheet-like surface, not as a widget overlay.
