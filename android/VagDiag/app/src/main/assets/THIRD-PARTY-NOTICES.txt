# Third-party notices

## Kotlin runtime

The Android APK contains Kotlin runtime components supplied by the Kotlin Gradle plugin 1.9.24, maintained by JetBrains and Kotlin contributors. Kotlin is licensed under **Apache License 2.0**. Source and licensing provenance: <https://github.com/JetBrains/kotlin/tree/v1.9.24/license>.

The full upstream license text is retained in [licenses/APACHE-2.0.txt](licenses/APACHE-2.0.txt). The included JetBrains annotations 13.0 dependency is also Apache-2.0: <https://github.com/JetBrains/java-annotations>. The APK embeds this license text, project NOTICE and these third-party notices in its assets.

## Development-only dependencies

JUnit 4.13.2 (EPL-1.0) and Hamcrest Core 1.3 (BSD-3-Clause) are resolved for JVM tests and are not packaged as app runtime dependencies. Android SDK/build tools, Gradle 8.7, Android Gradle Plugin 8.5.2, and GitHub Actions are build prerequisites; their distributions and licenses are separate and are not bundled in this repository or release.

## Related projects — not bundled

USBPcap-ARM64 retains GPL-2.0 for its driver and BSD-2-Clause for its CLI. VCDS-ARM64's original documentation retains MIT. These repositories are linked, not copied into this distribution.

Ross-Tech VCDS and interface firmware remain proprietary. No such software, loader, activation bypass, or driver is redistributed. Observed product names and minimal identification text appear only as descriptive interoperability metadata; they do not imply endorsement, licensing rights, or authenticity.
