# Agent instructions

Read these files before changing code:

1. `README.md`
2. `ARCHITECTURE.md`
3. `STATUS.md`
4. `ROADMAP.md`
5. Relevant file under `docs/`
6. Actual source tree and tests

## Rules

- Preserve the authorized security-research purpose of the project.
- Do not mark placeholders as completed features.
- Update `STATUS.md` whenever implementation state materially changes.
- Use `- [ ]` / `- [x]` checklists for project tracking.
- Keep WPS PIN algorithms deterministic and unit-tested.
- Add provenance/reference notes for every imported or reimplemented algorithm.
- Do not couple Compose UI directly to root shells or native process execution.
- Never assume root, monitor mode, or injection support.
- Pin third-party revisions; do not silently track a moving branch.
- Document licenses before adding third-party native source.
- Prefer small, reviewable commits.
- Build/test before claiming **Implemented AND verified**.

## Definition of done

A feature is done only when:

- [ ] implementation exists
- [ ] failure paths exist
- [ ] tests or a documented verification method exist
- [ ] documentation is updated
- [ ] `STATUS.md` is updated
- [ ] CI passes where applicable
