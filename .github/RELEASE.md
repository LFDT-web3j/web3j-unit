# Web3j-unit releases and rehearsals

The Release workflow validates the selected commit with the reusable Build
workflow, then stages the single root Maven publication,
`org.web3j:web3j-unit:<version>`. `verifyReleaseArtifacts` checks its configured
coordinates and local staging repository and requires five nonempty files:
the binary, sources and Javadoc JARs, POM and Gradle module metadata.

Verification reads existing files without building or publishing them. It runs
every time, removes a stale inventory before checking, and writes
`release-expected-files.txt` only after all checks pass. If staging and verification
are requested together, verification runs after staging. It does not support
SNAPSHOT filenames or silently include new publishing subprojects.

## Rehearse before merging

PRs to `main` touching the workflow, build configuration or sources automatically
run a non-publishing rehearsal. It tests GitHub's PR merge commit; the exact SHA
is saved in `release-metadata.txt`. Once this workflow is on the default branch,
**Actions → Release → Run workflow** can rehearse any selected branch. A manual
run is always a rehearsal, including when selecting a `release/*` branch.

Rehearsals use `0.0.0-rehearsal.<run-id>.<attempt>`. The `rehearsalVersion` Gradle
property changes only the publication version, after the checkout's Web3j
core/evm dependency version has been captured. It must not make Gradle resolve
those dependencies at the synthetic rehearsal version.

```sh
VERSION=0.0.0-rehearsal.local
./gradlew "-PrehearsalVersion=$VERSION" publishMavenPublicationToLocalStagingRepository --no-build-cache
./gradlew "-PrehearsalVersion=$VERSION" :verifyReleaseArtifacts --no-build-cache
```

The workflow records SHA-256 hashes and uploads
`release-artifacts-<version>-<attempt>`, containing the five required files,
checksums, inventory and source metadata. A separate job downloads that exact
artifact ID and checks the hashes, file set and source metadata again. Artifacts
request 14 days of retention, subject to repository limits.

A rehearsal self-test withholds the `.module` file. Verification must reject it,
leave no inventory, and not rebuild it. After restoration, verification and the
original payload checksums must pass again. The expected Gradle failure in this
step's log is part of the test; the step must finish successfully.

Rehearsals receive no publication/signing secrets and skip Maven Central deployment
and GitHub release/tag creation. They establish build, packaging, verification and
artifact-transfer behavior, not production signing or service acceptance. Build
validation uses the existing cache policy; staging disables task-output caching.
Normal PR Build and release validation both run intentionally.

This Kotlin-only project currently has no configured Kotlin API documentation
generator or git-publish destination. Its existing Javadoc JAR is packaged, but
contains no generated Kotlin API documentation. There is no separate documentation
publication job; `core:gitPublishPush` belongs to the core repository and cannot run
here. Kotlin API documentation is separate follow-up work.

Do not require path-filtered Release jobs globally: they do not run for unrelated
PRs. The regular Build workflow remains independent.

## Publish a release

Only a push to `release/<version>` enables publication, after validation and local
artifact verification. `-Pversion=<version>` selects both the publication and the
core/evm dependencies for a live release, preserving the existing release behavior.
Those dependency versions must already exist. Use a non-SNAPSHOT version such as
`6.0.0` or `6.0.0-rc.1`. Hyphenated versions are marked prerelease on GitHub.
An existing `v<version>` tag must resolve to the tested SHA; annotated tags are
dereferenced before comparison.

Central deployment uses the recorded staging files without re-running staging,
with checksums checked before and after deployment. Signing may add `.asc` files.
The GitHub release attaches the verified downloaded JARs and targets the tested
SHA. Its tag is checked again immediately before release creation.

A release branch executes its own committed workflow. Ensure the intended commit
contains this fix before pushing. A passing rehearsal does not authorize a live
release or validate production credentials.

## Recover a failed live release

Publication is not transactional: Central can succeed before the GitHub job fails.
Preserve the source SHA, version, artifact ID, files and checksums, logs, tag SHA,
Central deployment status and `build/jreleaser/output.properties`. An upload may
exist even if the job timed out without saving its deployment ID.

- If Central succeeded and only GitHub failed, reconcile Central coordinates and
  the tag/assets first. **Re-run failed jobs** is appropriate only if it excludes
  the successful Central job and retains its original artifacts.
- If the deployment job failed or timed out, do not blindly rerun it. Inspect the
  Portal state and recover only verified missing work through maintainer review.
  Published coordinates cannot be overwritten.
- If a tag moved or a GitHub release already exists, reconcile it rather than
  force-moving/deleting it or redeploying Central to repair GitHub metadata.
- Never use **Re-run all jobs** to recover a live publication.

See [RELEASE-DESIGN.md](RELEASE-DESIGN.md) for remaining publication controls.

## Snapshots

Snapshot runs are serialized. Only successful main-push Builds qualify; their SHA
is checked against current `main` before setup and again before publishing. Stale
runs skip. Main can still advance during upload; this is not an atomic branch lock.
