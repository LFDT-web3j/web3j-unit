# Web3j-unit release follow-ups

The current single-module workflow and recovery procedure are documented in
[RELEASE.md](RELEASE.md). These proposals are not implemented by the rehearsal fix
and do not authorize publication or repository policy changes.

## Publication recovery and approval

Central deployment precedes GitHub release creation. A later failure therefore
needs resumable completion using the original artifacts, rather than another
full deployment. Preserve deployment IDs, source SHA, version, payload checksums
and logs; reconcile remote state after a timeout before retrying.

Consider separating Central upload/validation from its final publication, and
preparing the GitHub metadata first. A draft GitHub release is not a tag reservation:
recheck the exact tag and protect it from unrelated changes. Web3j-unit has one
root publication; the core SDK's multi-module aggregation design does not apply.

A protected `release` environment with required reviewers and release-branch
restrictions would add an approval boundary. Move publishing secrets into it only
after an administrator decision. Split live publication into separate jobs first,
so PR/manual rehearsals continue without production secrets or approvals. Event
guards alone do not constrain writers who can edit workflows.

## Kotlin API documentation

This project contains Kotlin sources; Gradle's Java `javadoc` task has no Java
sources to document. A future change should add a Kotlin-aware generator, verify
the generated API content, and wire that output into the Maven Javadoc JAR.

Standalone documentation hosting needs an explicitly chosen destination and
reviewed authentication/identity setup. There is currently no configured
`gitPublish` destination in this repository. Do not copy the core SDK's
`core:gitPublishPush` task or assume generating docs proves publishing access.
