# Measurable detail-page Entry action

Status: needs-triage

## Incoming request

The large action button on the habit detail page does not feel right visually. For measurable habits, it should not present the current add/increment affordance as the primary detail-page action. Use an Entry value-edit action instead, with the same clear selected-date/value/Target context as the homepage editor where appropriate.

## Scope to clarify during triage

- Which detail-page surfaces are affected: hero action only, history cells, or both.
- Whether the action should open the existing measurable Entry editor or a detail-specific editor.
- Whether boolean detail-page completion keeps its current check-off action.
- How the action should behave for today versus a historical date.
- The visual treatment of the large action button after the measurable action changes.

## Constraints

- Do not change persistence or scoring semantics as part of triage.
- Preserve the selected-date model and existing detail history behavior until the ticket is specified.
- Do not implement from this raw request; graduate it through the approved Matt flow if it becomes multi-session work.
