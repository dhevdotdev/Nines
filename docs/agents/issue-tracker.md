# Issue tracker: Local Markdown

Issues and specs for this repo live as markdown files in `.scratch/`.

## Conventions

- One feature per directory: `.scratch/<feature-slug>/`
- The spec is `.scratch/<feature-slug>/spec.md`
- Implementation issues are one file per ticket at `.scratch/<feature-slug>/issues/<NN>-<slug>.md`, numbered from `01` — never a single combined tickets file
- Triage state is recorded as a `Status:` line near the top of each issue file (see `triage-labels.md` for the role strings)
- Comments and conversation history append to the bottom of the file under a `## Comments` heading

## When a raw idea graduates into a spec + tickets

This is a local-repo convention, not something Matt Pocock's skills specify — the local-markdown tracker's numbering rules are thinner than the GitHub/GitLab case (where issue numbers are platform-assigned and this situation can't arise) and don't cover it.

A raw backlog idea starts life at `.scratch/<feature-slug>/issues/01-<slug>.md`, `Status: needs-triage`. Most ideas stay small enough that `/triage` handles them entirely in place — grill if needed, attach an agent brief, update the `Status:` line on that same file. No new files, no renumbering, nothing below applies.

If an idea instead grows into a full `/to-spec` → `/to-tickets` build, the raw idea file gets superseded by `spec.md` (which fully captures it) and a new `issues/01-NN-<slug>.md` sequence of real tracer-bullet tickets. When that happens:

- Move the original raw idea file out of `issues/` to `.scratch/<feature-slug>/idea.md` — kept for history, but no longer part of the ticket-numbering sequence.
- Number the real tickets `01`, `02`, ... cleanly from the start, per the normal convention above.
- Update the moved idea file's `Status:` to `superseded (see below)` and note where the spec/tickets live.

## When a skill says "publish to the issue tracker"

Create a new file under `.scratch/<feature-slug>/` (creating the directory if needed).

## When a skill says "fetch the relevant ticket"

Read the file at the referenced path. The user will normally pass the path or the issue number directly.

## Wayfinding operations

Used by `/wayfinder`. The **map** is a file with one **child** file per ticket.

- **Map**: `.scratch/<effort>/map.md` — the Notes / Decisions-so-far / Fog body.
- **Child ticket**: `.scratch/<effort>/issues/NN-<slug>.md`, numbered from `01`, with the question in the body. A `Type:` line records the ticket type (`research`/`prototype`/`grilling`/`task`); a `Status:` line records `claimed`/`resolved`.
- **Blocking**: a `Blocked by: NN, NN` line near the top. A ticket is unblocked when every file it lists is `resolved`.
- **Frontier**: scan `.scratch/<effort>/issues/` for files that are open, unblocked, and unclaimed; first by number wins.
- **Claim**: set `Status: claimed` and save before any work.
- **Resolve**: append the answer under an `## Answer` heading, set `Status: resolved`, then append a context pointer (gist + link) to the map's Decisions-so-far in `map.md`.
