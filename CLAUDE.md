# CLAUDE.md

The working rules for the island are in [Rule_Book.md](Rule_Book.md). `CLAUDE_RULES.md` no longer exists.

## graphify

Use graphify to navigate this repo. A knowledge graph of the project is in `graphify-out/` (`graph.json`, `GRAPH_REPORT.md`, `graph.html`; the folder is git-ignored and local to each machine).

- **Navigate with the graph first.** For any codebase question (where something lives, what calls what, how a feature is wired), run `/graphify query "<question>"` (or `/graphify path "A" "B"`, `/graphify explain "X"`) before reading files or grepping, then open only the files the graph points to. Skim `graphify-out/GRAPH_REPORT.md` for the god nodes and communities when starting unfamiliar work.
- **Update the graph after every major change.** After a major change (a new feature or screen, a refactor, files added, moved or deleted, a merge from upstream, or a large fix), run `/graphify . --update` before finishing the task, so the next session starts from an accurate map. Small edits inside a single function do not need an update.
- **If there is no graph yet** (a fresh clone, or `graphify-out/` is missing), build it once with `/graphify .`.
- The graph is a navigation aid, not the source of truth: confirm details in the code before changing it, and trust `Rule_Book.md` for behaviour rules.

## Alert rules

How the island picks the main pill, the companion bubble and what expires (tiers, two-slot cap, 7s/5s timers,
message grouping) is documented in [Rule_Book.md](Rule_Book.md) (sections 1 and 3). Read it before changing
`app/src/main/java/com/agupta07505/smartisland/alert/AlertEngine.kt`, `ui/IslandViewModel.kt`, `ui/PillLayout.kt` or how
`service/SmartIslandOverlayService.kt` sizes the window; keep the doc and `alert/AlertEngineTest` in step with any behaviour change.

## Motion

Spring presets and the kinetic glow are documented in [Rule_Book.md](Rule_Book.md) (sections 2.2 and 2.3); change the maths in
`app/src/main/java/com/agupta07505/smartisland/ui/IslandMotion.kt` with its tests, not inline in the overlay view.
