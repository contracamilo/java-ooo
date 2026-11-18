# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Pure-Java learning project (object-oriented programming): a console streaming-platform simulator called "KELEA PLAY". No build tool (no Maven/Gradle), no dependencies, no tests yet. IntelliJ IDEA module (`java-ooo.iml`) with `src/` as the only source root. JDK 21 is installed locally.

## Commands

```bash
# Compile everything into out/ (gitignored)
javac -d out $(find src -name "*.java")

# Run the console app
java -cp out kelea.play.Main
```

There is no test suite or linter configured. If tests are added, prefer JUnit 5 and note the run command here.

## Architecture

Entry point is `kelea.play.Main`: an interactive console menu loop (`switch` on the option number) that operates on a single in-memory `Platform`. Data is not persisted; everything is lost on exit.

- `platform/Platform` — owns the catalog (`List<Movie>`); add/remove/list operations live here, not in `Main`.
- `content/Content` — `abstract sealed` base class (permits `Movie`, `Documentary`), immutable (final fields, no setters; a record can't extend a class). Accessors keep record style: `title()`, `note()`. Equality is by title ignoring case. The catalog, `ConsoleUtils` and `Platform` work with `Content`; behavior that differs per kind (`play()`, `type()`) is polymorphic. Switches over a `Content` use pattern matching and need no `default` because it is sealed. To add a new kind: extend `Content`, add it to `permits` and to `ContentType`, then the compiler flags every switch that must handle it.
- `User/User` — user model (`watch(Movie)`); not wired into `Main` yet.
- `util/ScannerUtils` — the only place that reads `System.in`. It shares one static `Scanner`, and `catchNumber`/`catchDecimals` re-prompt until the input parses. Never create another `Scanner` on `System.in`; use these helpers so input stays consistent.

Menu options are the `MenuOption` enum: its declaration order defines the numbers shown, and `Main` switches on it. Genres are the `Genre` enum (never a free String); read enums from the console with `ScannerUtils.catchEnum`. All console output formatting goes through `ConsoleUtils`; the catalog is loaded at startup from `data/movies.csv` (`repository/ContentRepository` is the storage interface (load/append/delete) used by `Main`; `repository/FileContentRepository` implements it and knows the line format (`title;genre;language;quality;duration;note[;type[;director|topic[;narrator]]]`, 6 fields = legacy movie with unknown director) and delegates all disk work to the generic `util/FileUtils` (read/append/replace/delete lines; NIO `Path`/`Files`); Add appends one line and Delete removes one line right away, other lines (comments, unreadable ones) are left untouched; paths are relative to the working directory, so run from the project root).

## Conventions

- Because this is a learning project, add concise comments to code that is non-obvious or conceptually tricky (e.g. constructor chaining, validation side effects, why a design choice was made). Skip comments that just restate the code.
- Use modern Java (21): `switch` arrow syntax, text blocks, `record`s for plain data where it fits, `var` only when the type is obvious, `Optional` for "may not exist" lookups, streams where they read better than loops.
- Keep fields `private` with getters/setters as needed (`Movie` already follows this; `Platform` still has public fields and should be tightened when touched).
- Package `kelea.play.User` has an uppercase name, which breaks Java's lowercase-package convention; keep it consistent with the directory unless doing a deliberate rename (`user`).
