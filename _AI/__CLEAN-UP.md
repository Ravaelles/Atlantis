# Working note: shrink the October comments

Owner's request (kept as a working note; the `__` prefix marks it as not part of
the reference set): the October changes added very long comments, several of them
for bugs that were already fixed. Over time they will bloat the source. Go through
the changes made since the base branch `feature/2025-12-t` and **replace long
comments about already-fixed bugs with much shorter ones** - usually one or two
sentences is enough. If a comment is genuinely needed, move it to the method's
javadoc instead of leaving it inline.

The example that started it:

```java
if (isWoundedAndInDanger()) return false;
```

This wants a short javadoc line on `isWoundedAndInDanger`, not a 15-line inline
comment.

Scope: all changes from the last two weeks (500+ files). Work in two phases:

- **Phase A** - list every touched file. Names only, nothing else.
- **Phase B** - one file at a time: read the comments, shorten as described, keep
  each change reviewable and commit in small steps.

Language: English, per `_AI/CONVENTIONS.md` §1 (this note included).
