# Tested gesture policy

**Status:** superseded

The homepage's gesture implementation already has deliberate mutually-exclusive reorder and swipe modes, but the policy is embedded in Compose. Extract the meaning and selected-date gate into a pure contract while keeping pointer mechanics and the existing ADR intact.
