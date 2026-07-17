<!--
SPDX-FileCopyrightText: Copyright Contributors to the GXF project

SPDX-License-Identifier: Apache-2.0
-->

# Dependency audit

This document records the outcome of a **read-only** dependency and plugin audit of the
Open Smart Grid Platform (OSGP) Maven estate. It is intended as input for follow-up work;
**no dependencies or plugins were upgraded** as part of producing this report.

Automated dependency update PRs are already configured through
[`.github/dependabot.yml`](../.github/dependabot.yml) (daily `maven` scans against `/`,
`open-pull-requests-limit: 10`, plus grouped weekly `github-actions` updates). The findings
below are meant to prioritise the manual review of the Dependabot PRs and to flag
end-of-life (EOL) / abandoned libraries that Dependabot cannot always upgrade safely.

## How this was generated

Run against the `development` branch with JDK 17 and the `pl,sm` profiles:

```bash
./mvnw -B -Ppl,sm versions:display-dependency-updates
./mvnw -B -Ppl,sm versions:display-plugin-updates
```

Notes on method:

* `versions:display-dependency-updates` completed successfully. Because the reactor imports
  several third-party BOMs (Apache Camel, ActiveMQ, Spring, Reactor, AWS SDK, Flyway, …),
  the raw output contains ~1,980 unique "managed dependency" update lines, the vast majority
  of which are transitive versions governed by those imported BOMs rather than versions OSGP
  pins directly. The tables below therefore focus on the versions **OSGP controls itself**
  via `super/pom.xml` (`<properties>` and `<dependencyManagement>`).
* `versions:display-plugin-updates` fails on the full reactor with
  `Cannot invoke "String.length()" because "end" is null` (a known NPE in
  versions-maven-plugin `2.10.0`). Plugin versions below were read directly from
  `super/pom.xml` properties and cross-checked manually.
* Pre-release candidates (`-M*`, `-RC*`, `-beta`, `-alpha`, `*-dev`, date-stamped snapshots)
  reported by the plugin are intentionally **excluded** from the "recommended" column; only
  stable GA targets are listed.

## Directly-pinned dependencies worth reviewing

Versions are taken from `super/pom.xml`. "Latest (stable)" reflects the newest GA release
reported by `versions:display-dependency-updates` at audit time.

| Dependency | Current | Latest (stable) | Notes |
| --- | --- | --- | --- |
| `com.google.protobuf:protobuf-java` | 2.4.1 | 3.x / 4.x | **Critically old (2011).** Retained for legacy OSLP wire compatibility; a major-version bump needs generated-code regression testing. Highest-priority item to plan. |
| `xmlunit:xmlunit` | 1.6 | 1.x line EOL | **EOL.** The 1.x branch is abandoned; the maintained successor is `org.xmlunit:xmlunit-*` (2.x). Test-only dependency. |
| `axion:axion` | 1.0-M3-dev | — | **Abandoned project** (Axion DB, no releases since ~2005). Test-scope only; candidate for removal/replacement (e.g. H2/Testcontainers). |
| `io.cucumber:cucumber-*` | 5.7.0 | 7.34.4 | Two majors behind (5.x is from 2020). Integration-test only; upgrade to 7.x involves API/step changes. |
| `org.mockito:mockito-core` | 4.8.0 | 5.x | One major behind; 4.x no longer actively maintained. Test-only. |
| `net.sf.ehcache`/`ma.glasnost.orika:orika-*` | 1.5.4 | 1.5.4 (maintenance) | Orika is effectively unmaintained (last GA 2019). Long-term: plan migration to an actively maintained mapper. |
| `com.fasterxml.jackson.core:jackson-*` | 2.15.3 | 2.18+ | 2.15.x has known advisories; align to a recent 2.18/2.19 patch line. |
| `org.flywaydb:flyway-core` | 9.22.3 | 10.x / 11.x | Major line behind (9.x). Newer majors change baseline/edition behaviour — review before upgrading. |
| `org.hibernate.orm:hibernate-*` | 6.3.1.Final | 6.6.x | Within the same major; a 6.6.x patch bump is low risk. |
| `com.zaxxer:HikariCP` | 3.4.5 | 5.x | Old (2020). 5.x targets JDK 11+ and is a straightforward runtime upgrade to validate. |
| `com.google.guava:guava` | 32.1.2-jre | 33.6.0-jre | Minor/feature updates; low risk. |
| `org.postgresql:postgresql` | 42.7.8 | 42.7.13 | Patch releases with CVE/bugfixes; low risk. |
| `ch.qos.logback:logback-*` | 1.5.21 | 1.5.38 | Patch releases; low risk. |
| `commons-io:commons-io` | 2.16.1 | 2.22.0 | Minor updates; low risk. |
| `org.apache.activemq:*` | 6.1.6 | 6.2.7 | Same major; patch/minor bump. |
| `joda-time:joda-time` | 2.14.0 | 2.14.2 | Patch; consider migrating remaining Joda usage to `java.time`. |
| `com.microsoft.azure:msal4j` | 1.23.1 | 1.30.x | Feature/security updates within 1.x. |

## Plugin versions worth reviewing

Read from `super/pom.xml` properties (the `display-plugin-updates` goal errors on the full
reactor — see method note above).

| Plugin | Current | Notes |
| --- | --- | --- |
| `com.mycila:license-maven-plugin` | 3.0 | Several majors behind (latest 5.x). Enforces SPDX headers; validate header handling before upgrading. |
| `com.github.spotbugs:spotbugs` (+ maven plugin) | 4.6.0 / 4.6.0.0 | Newer 4.x patch line available (≈4.10.x). |
| `com.spotify.fmt:fmt-maven-plugin` | 2.29 | Current-ish; drives google-java-format `check`/`format`. |
| `org.codehaus.mojo:versions-maven-plugin` | 2.10.0 | The `display-plugin-updates` NPE is a bug in this version; a newer release may fix it. |
| `maven-site-plugin` / `maven-jxr-plugin` / `maven-project-info-reports-plugin` | 3.21.0 / 3.3.2 / 3.9.0 | Reporting-only; not on the critical build path. |

## Transitive / BOM-managed versions

Versions for Apache Camel (`4.8.3`), ActiveMQ (`6.1.6`), the Spring / Reactor / AWS families,
and the Flyway database drivers are largely governed by the third-party BOMs imported in
`super/pom.xml` rather than pinned individually. Upgrading those is best done by bumping the
imported BOM version (which Dependabot proposes) and re-running
`./mvnw -T1C -B -Ppl,sm install`, rather than overriding individual transitive versions.

## Recommended follow-up (no changes made here)

1. Triage the open Dependabot `maven` PRs, prioritising security-relevant patch bumps
   (jackson, postgresql, logback, commons-io) first — these are low risk.
2. Schedule dedicated work for the EOL / abandoned items that Dependabot cannot bump safely:
   `protobuf-java` 2.4.1, `xmlunit` 1.6, `axion`, `orika`, and Cucumber 5 → 7.
3. Track the `versions:display-plugin-updates` NPE (upgrade `versions-maven-plugin`) so plugin
   updates can be audited automatically in future runs.
