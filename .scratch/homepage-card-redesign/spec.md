Status: ready-for-agent

# Homepage habit card redesign

## Problem Statement

After date-aware homepage work removed the redundant per-row history strip, the main habit cards lost a useful visual rhythm and now feel empty. The numeric exact-edit control was then made more prominent as an isolated button, but it now competes too strongly with the primary logging action and makes numeric cards feel assembled from unrelated controls.

The homepage needs to feel recognizably Material 3 Expressive while remaining a fast daily logging surface. It must communicate the selected date's Entry clearly, preserve Nines' Score and Streak concepts, and make related actions feel like one coherent card interaction rather than a collection of floating buttons.

## Solution

Rebuild the homepage habit card as a stable, compact horizontal expressive tile. Keep the Score ring and habit icon as the visual anchor, place the habit name and selected-date status together in the middle, and organize the related actions into one expressive action group at the trailing edge.

Use one action grammar across habit types:

- Boolean habits expose a primary completion action and a secondary habit-configuration edit action.
- Numeric habits expose a primary increment action and a secondary exact-value edit action.

The selected-date status area is one intentional visual unit, not a replacement history strip or a pile of pills. Boolean habits show their selected-date state and Streak when available. Numeric habits show value, Target, unit, and Streak when available. Missing information uses an intentional placeholder such as `Not logged` or `No target`; the status area does not disappear.

The same actions remain available for today and past selected dates. The existing non-today indicator provides date context; historical correction does not become read-only or visually second-class.

The card keeps stable geometry. A completion changes local action-group shape, status treatment, and Score feedback, but does not change card height or reorder the list. Material 3 Expressive shape and motion language supplies the personality, while a measurable progress hierarchy keeps the card useful rather than game-like.

## User Stories

1. As a user logging a habit, I want the primary logging action to be immediately obvious, so that I can act without decoding the card.
2. As a user viewing my habits, I want each card to have a recognizable expressive identity, so that the list feels alive rather than like a plain data table.
3. As a user, I want the Score ring and habit icon to remain a stable visual anchor, so that I can recognize habits quickly.
4. As a user, I want the selected-date Entry state to occupy a meaningful status area, so that the card still feels complete after the history strip is removed.
5. As a user viewing a boolean habit, I want to see whether the habit is done for the selected date, so that the card reflects the date I am actually viewing.
6. As a user viewing a boolean habit, I want to see its Streak when one exists, so that recent consistency remains visible without a redundant history strip.
7. As a user viewing a numeric habit, I want to see its current value against its Target and unit, so that progress is understandable at a glance.
8. As a user viewing a numeric habit, I want to see its Streak when one exists, so that progress and consistency are both legible.
9. As a user viewing a new habit with no Entry, I want an intentional `Not logged` state, so that the card does not look broken or collapse unpredictably.
10. As a user viewing a numeric habit without a Target, I want an intentional `No target` state, so that missing configuration is understandable.
11. As a user with a numeric habit, I want quick increment and exact-value editing to feel related, so that the controls do not compete as unrelated buttons.
12. As a user with a boolean habit, I want completion and habit configuration to follow the same action grammar as numeric habits, so that the card family feels coherent.
13. As a user, I want the primary logging action to have stronger emphasis than configuration/editing, so that the most common action wins visually.
14. As a user, I want the action group to respond to pressing with expressive shape or width motion, so that interactions feel tactile and intentional.
15. As a user, I want completed state to produce local visual feedback, so that I know the Entry was accepted.
16. As a user logging several habits in sequence, I want cards to keep their geometry and list position, so that the list does not jump after every action.
17. As a user correcting history, I want the same action group to work on a past selected date, so that historical correction is as fast as today's logging.
18. As a user, I want the visible date context to remain outside the card's action hierarchy, so that past-date editing is clear without making every card look disabled.
19. As a user with a long habit name, I want the name to truncate gracefully without moving the actions, so that the card remains scannable.
20. As a user with a long numeric value or unit, I want the status to truncate gracefully without shrinking the action hit areas, so that controls remain usable.
21. As an accessibility user, I want each action to have a clear semantic label, so that I can distinguish logging, exact editing, and habit configuration.
22. As a user in light or dark dynamic color, I want the card's status and action states to retain readable contrast, so that expressive color never obscures meaning.
23. As a maintainer, I want the card's visual hierarchy to be shared across boolean and numeric habits, so that future habit types have a clear extension point.
24. As a maintainer, I want this redesign to preserve the existing date-aware state and gesture behavior, so that visual improvements do not regress historical editing, swipe actions, or reorder gating.

## Implementation Decisions

- The homepage card is a fast daily logging surface with an expressive identity, not a detail-screen replacement.
- The card uses a stable compact horizontal anatomy: Score/icon anchor, name plus selected-date status, and one trailing action group.
- Score remains a persistent derived domain signal and the left visual anchor. The redesign does not change Score calculation or storage.
- Selected-date status is a stable slot whose content adapts to habit type. The slot remains present for missing data.
- Boolean and numeric habits share an action grammar, but their primary actions remain type-specific.
- The primary logging action is visually stronger than the secondary configuration/exact-edit action.
- The final ButtonGroup item arrangement is an implementation decision constrained by this spec; it must read as one related group rather than two isolated floating buttons.
- Material 3 Expressive interaction, shape, and motion mechanisms are preferred. Pressed feedback stays local to each action in this compact group; width redistribution is intentionally avoided because the actions are independent and the card geometry must remain stable. Completion may still morph local action/status shapes.
- Card geometry and list order remain stable during state changes. No completion-driven reordering or card collapse is introduced.
- The same action behavior applies to today and historical selected dates.
- Swipe-to-archive and swipe-to-edit remain habit-level actions and stay available outside reorder mode, regardless of the selected date.
- The existing reorder-mode gating remains: reorder is available only for today's selected date and exits when a past date is selected.
- The habit detail screen's history/calendar view is unchanged.
- No database schema or repository contract changes are required by this visual redesign.

## Testing Decisions

- The primary seam is the observable homepage UI: the screen and habit row rendered on-device. Verify external behavior, visual hierarchy, action semantics, selected-date behavior, accessibility labels, and motion rather than implementation details.
- No new Compose UI-test infrastructure is introduced. The project deliberately uses manual on-device verification for UI/ViewModel wiring and has no established UI test suite for this surface.
- Existing pure scoring tests remain the prior art for dependency-free domain logic. This redesign does not add new scoring or visibility rules, so no new pure domain test seam is required.
- Manual verification covers boolean and numeric cards, empty/missing data, long names/values, today and past selected dates, light/dark dynamic color, action-group press/completion motion, accessibility labels, swipe actions, and reorder-mode gating.
- The final implementation must compile and pass the full existing Gradle test suite before on-device review.

## Out of Scope

- Changing Score, Streak, Gap, Entry, Schedule, or Target calculations.
- Adding frequency-based due-date filtering.
- Restoring the homepage five-dot history strip.
- Changing the habit detail screen's history/calendar view.
- Changing archive, edit, or reorder domain behavior beyond preserving their existing selected-date rules.
- Adding a new database schema, repository method, or persistence field.
- Introducing a new UI-test framework solely for this redesign.
- Copying source code from reference apps. Reference apps are used only for visual hierarchy and interaction ideas, consistent with the existing reference-app ADR.

## Further Notes

This spec follows the accepted design discussion recorded during the `/grill-with-docs` phase. The visual direction combines Material 3 Expressive component/motion language with a measurable progress hierarchy: expressive enough to feel alive, but not game-like or decorative for its own sake.

The feature should be implemented as fresh tickets under this feature directory. The older date-aware homepage tickets remain historical records of the already completed date-navigation work.
