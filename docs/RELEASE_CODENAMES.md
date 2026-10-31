# Froodz VAG Diag — Automotive Release Codenames (0.1–4.0)

> **Approved codename roadmap** · Automotive components, diagnostic methods, vehicle networks, and performance terminology<br>
> **Scope:** Froodz VAG Diag application · **Coverage:** 24 named versions, from **0.1 Ignition** to **4.0 Overdrive**<br>
> **Document status:** Approved naming policy with verified build/release alignment · checked **2026-10-31**<br>
> **Current local build:** **0.1.4-usb-fix-dev · Ignition · Alpha development build** · not published to GitHub<br>
> **Naming theme:** *Understand the machine. Diagnose with precision. Evolve with performance.*

## 1. Purpose and identity

Froodz VAG Diag uses a release-naming tradition based on **real automotive engineering concepts**. Each codename is selected to be memorable to enthusiasts and technically meaningful to technicians. The sequence deliberately mixes:

- **Engine and combustion:** ignition, spark, piston, crankshaft, camshaft, injector, knock, and fuel trim.
- **Diagnostics and networking:** lambda sensing, K-Line, CAN bus, freeze-frame data, gateway modules, UDS, and CAN FD.
- **Powertrain and performance:** torque, turbocharging, spool, wastegate, dual-clutch gearboxes, launch control, redline, and overdrive.

The names are **the selected roadmap identities**. Section 2.1 matches them to actual retained APKs, local development builds and published prereleases. All other milestones remain planned. The suggested engineering directions below describe a coherent development story; they are **not feature commitments, compatibility claims, or release dates**. Actual scope belongs in each version's issue tracker, acceptance tests, release notes, and support matrix.

> **Branding note:** “VAG” refers here to the Volkswagen Group ecosystem. Froodz VAG Diag is not represented by this document as an official Volkswagen, Audi, Škoda, CUPRA, or SEAT product or as an authorized diagnostic service.

## 2. Official version and codename roadmap

| Version | Official codename | Automotive origin | Intended stage | Identity / release theme |
|---|---|---|---|---|
| **0.1** | **Ignition** | Ignition system and engine start | Alpha | First spark; foundations |
| **0.2** | **Spark** | Spark plug and ignition discharge | Alpha | First useful diagnostic signals |
| **0.3** | **Piston** | Reciprocating engine piston | Alpha | Repeatable diagnostic cycles |
| **0.4** | **Crankshaft** | Engine crankshaft and RPM | Alpha | Timing and measurement discipline |
| **0.5** | **Lambda** | Oxygen sensing and air–fuel control | Beta | Interpretation of live measurements |
| **0.6** | **K-Line** | Legacy single-wire diagnostic communication | Beta | Older vehicle connectivity theme |
| **0.7** | **Injector** | Electronic fuel injection | Beta | Precise, structured control and reporting |
| **0.8** | **CANbus** | Controller Area Network | Beta | Multi-module communications theme |
| **0.9** | **Synchro** | Engine phase and timing synchronization | Release candidate | Consistency and readiness |
| **1.0** | **Torque** | Rotational force in a powertrain | First stable | Dependable diagnostic foundation |
| 1.1 | Camshaft | Valve timing and cam position | Stable update | Deeper system interpretation |
| 1.2 | Knock | Combustion knock sensing | Stable update | Fault intelligence and protection |
| 1.3 | FreezeFrame | Recorded conditions at a diagnostic event | Stable update | Preserve diagnostic context |
| 1.4 | FuelTrim | Adaptive fuel correction | Stable update | Trend analysis and explainability |
| **2.0** | **Turbo** | Exhaust-driven forced induction | Second generation | Speed and expanded capability |
| 2.1 | Spool | Turbo acceleration and boost buildup | Stable update | Faster response and live-data experience |
| 2.2 | Wastegate | Turbo boost regulation | Stable update | Control-system insight and limits |
| 2.3 | DualClutch | Dual-clutch gearbox technology | Stable update | Powertrain diagnostics breadth |
| 2.4 | LaunchControl | Controlled high-performance launch | Stable update | Guided, deliberate workflows |
| **3.0** | **Redline** | Upper engine-speed operating boundary | Third generation | Precision at the edge of complexity |
| 3.1 | Gateway | Vehicle network gateway | Stable update | Vehicle-wide topology and coordination |
| 3.2 | UDS | Unified Diagnostic Services | Stable update | Modern diagnostic-service architecture |
| 3.3 | CAN-FD | CAN with Flexible Data-Rate | Stable update | Forward-looking network transport |
| **4.0** | **Overdrive** | Gear ratio below 1:1, often for cruising | Fourth generation | Mature, efficient, far-reaching platform |

**Count:** 24 names. The exact codenames and their assigned versions are selected; implementation details and dates remain open until separately planned and tested.

The **Intended stage** column is a roadmap target, not an assertion that the version has reached that stage. Actual artifact and publication status is recorded below. The names, capitalization, hyphens and version assignments in the table are unchanged.

### 2.1 Verified alignment with actual builds and releases

**Snapshot: 2026-10-31.** The GitHub release list and the published APK and source ZIP for each prerelease were checked read-only; downloaded hashes matched GitHub's asset digests. Local rows were checked against APK manifests, retained source snapshots and frozen verification receipts. These are distinct evidence classes, not one chronological public release stream.

| Exact APK versionName / artifact | Codename | Android versionCode | Actual status | Verified scope / qualification boundary |
|---|---|---:|---|---|
| `0.1.0` · `VagDiag-bench.apk` | **Ignition** | 1 | Historical retained local APK; not a current recommendation | Bench-development artifact. Exact bytes identified below; not evidence of qualified Android/vehicle diagnostics. |
| `0.1.0` · `VagDiag-kw1281.apk` | **Ignition** | 1 | Historical retained experiment; superseded | Different binary despite the same versionName/versionCode. Earlier Eos KW1281 coverage expectations were retracted; this is not a validated Eos scanner. |
| `0.1.1` · `VagDiag-0.1.1-debug.apk` | **Ignition** | 2 | [Published GitHub prerelease](https://github.com/Fredy-E/VAG-ARM64/releases/tag/v0.1.1), **2026-10-09 UTC** | Experimental off-vehicle USB bench prototype. Its notes explicitly exclude ECU diagnostics/DTC decoding and qualified physical Android USB/vehicle support. |
| `0.2.0-preview.1` · `VagDiag-0.2.0-preview.1-debug.apk` | **Spark** | 3 | [Published GitHub UI-preview prerelease](https://github.com/Fredy-E/VAG-ARM64/releases/tag/v0.2.0-preview.1), **2026-10-13 UTC** | Historical experimental UI preview, not a completed diagnostic milestone or a qualified OTG/vehicle build. Preserve its tag and binary assets; align its display title with the approved branding. |
| `0.1.2-workshop-dev` · retained pre-CAN snapshot APK (`before.apk`) | **Ignition** | 3 | Historical local Workshop build; confirmed in the **2026-10-30** pre-CAN snapshot | Native Workshop UI/logging baseline. Not a published GitHub release. Its original handoff date is not reconstructed from the backup date. |
| `0.1.3-can-foundation-dev` · `Froodz-VAG-Diag-0.1.3-can-foundation-dev.apk` | **Ignition** | 4 | Frozen and handed off locally **2026-10-31**; not published | MIT serial-library integration, receive-only SLCAN/passive TP2.0 foundation and synthetic replay. KW1281 vehicle operations quarantined; no live CAN session. |
| **`0.1.4-usb-fix-dev`** · **`Froodz-VAG-Diag-0.1.4-usb-fix-dev.apk`** | **Ignition** | **5** | **Current frozen local alpha development build**, handed off **2026-10-31**; not published | Find/Connect separation, permission recovery and USB inventory, button audit, export fixes. Physical S23 Ultra/cable operation and Eos ECU/DTC reads remain unverified. |

**Version-family rule:** `0.1.4-usb-fix-dev` belongs to **0.1 Ignition**. It is neither **0.4 Crankshaft** nor **1.4 FuelTrim**. Likewise, offline CAN-foundation work in `0.1.3` does not make it **0.8 CANbus**.

**Historical numbering divergence:** the public `0.2.0-preview.1` and the current local `0.1.x` workstream coexist. A numerically higher historical preview is not automatically the current engineering build or the better-supported artifact. Do not erase/relabel that preview, retroactively renumber local APKs, or invent a release to make the history look linear. Future tag selection must reconcile these streams explicitly.

**Android update identity:** `versionName` is the display/build label; `versionCode` is a separate Android update field. The legacy APKs reused code 1, and the UI preview and Workshop snapshot both use code 3. Those numbers alone do not uniquely identify bytes. Track filename, exact version, SHA-256 and signing identity together. Future update builds must use an appropriate increasing versionCode and compatible signing; no code/name is changed by this document.

Codename attribution here aligns documentation and release display names with the approved roadmap; it is **not a claim that historical APKs already embedded those names**. Frozen APKs, source ZIPs, checksums, tags and existing asset bytes remain unchanged. Release titles/descriptive branding may be updated under the owner's explicit publication approval without renaming or rebuilding those artifacts.

### 2.2 Current Ignition build — evidence and limits

Canonical documentation label:

```text
Froodz VAG Diag 0.1.4-usb-fix-dev Ignition — Alpha development build
```

- Package: `com.vagdiag`; Android versionCode **5**; development/debug signed. The current APK's manifest version remains exactly `0.1.4-usb-fix-dev`.
- **93 JVM tests**, **10 native Android instrumentation cases**, **112 visible WSA UI checks**, and **5 Python snapshot tests** passed for the frozen build. These are separate suites, not a count of supported vehicle functions.
- Actual document-picker Save/readback and Cancel/no-created-file were checked; the separate saved test log was **869 bytes**. One cosmetic `MonochromeLauncherIcon` lint warning remains; no lint errors.
- Installed WSA APK was pulled back and SHA-256 matched. WSA has no USB passthrough; connected-state callback fixtures are not physical USB grant/claim tests.
- Find is descriptor-only; Connect USB separately requests permission/opens the known interface. Permission wait has cancellation/timeout recovery and does not disable log export indefinitely.
- Intended physical setup: the owner's same VCDS cable, Galaxy S23 Ultra, owner-reported Android 17, USB-C-to-USB-A converter. This setup is **not yet qualified by a real phone log** at this snapshot.
- The separate CDC/SLCAN foundation is not an implementation of the existing cable's proprietary CAN session protocol. No active TP2.0 session, verified ECU identification/DTC read, controller scan, UDS or CAN-FD support is claimed.
- KW1281 vehicle actions remain quarantined; no coding, adaptation, fault clearing or firmware programming is enabled. The existing Windows VCDS/driver/loader/cable firmware was not changed.

The local predecessor `0.1.3-can-foundation-dev` has its own frozen evidence: **77 JVM tests**, **108 WSA UI checks** and **5 Python snapshot tests**. Do not attach the newer 0.1.4 test totals to the older binary.

### 2.3 Exact APK identity ledger

These hashes identify the verified retained/downloaded bytes. Listing a local file is **not** an announcement that it is uploaded, supported or recommended for vehicle use. File names and hashes are preserved, not changed to add codenames.

| Artifact / exact version | SHA-256 |
|---|---|
| `VagDiag-bench.apk` · `0.1.0` | `6e5f3a4178991529af7cd456b59008640a779a4eead1bed6725b225e7463bd3a` |
| `VagDiag-kw1281.apk` · `0.1.0` | `a4d53e93aba54a9e16f7b1c606ef59aeb79edb4771e143b636b2cea8e6393db6` |
| `VagDiag-0.1.1-debug.apk` · `0.1.1` | `59f25092a8b226045de73d3bbf0ecc2d722e1681bff7e7e77d1cf8a897cb7927` |
| `VagDiag-0.2.0-preview.1-debug.apk` · `0.2.0-preview.1` | `96befaede74fbd8001a11df788609126f0569a75b22d7ec181f95054c1d7ad07` |
| Pre-CAN snapshot `before.apk` · `0.1.2-workshop-dev` | `100a10e829f49940a43367e87fb85e607a2fbcfbf943c541bd74c0329594e1dd` |
| `Froodz-VAG-Diag-0.1.3-can-foundation-dev.apk` | `f33de006c5292d26fb9437934b63a0e30b5ed9cabfc9feb48e0ab2470a11c618` |
| **`Froodz-VAG-Diag-0.1.4-usb-fix-dev.apk`** | **`c1c966b33183612f8aa3e4ec901c64507efc47668a7d38a7bd36b2a0aeeedc4d`** |

The two public release pages linked above retain their original source-commit references and asset checksums. The local frozen builds retain their separate `verification.json`, `Verification.md` and `SHA256SUMS.txt` receipts. No stable release or physical vehicle support is established merely by an APK signature or a matching hash.

## 3. Flagship releases — the five major identities

### Froodz VAG Diag 0.1 — Ignition · Alpha

**Tagline:** *Every great diagnosis starts with a spark.*<br>
**Why this name:** Ignition is the event that initiates combustion in a spark-ignition engine. As a product metaphor it marks the first working application architecture, first reproducible build, and first steps toward communicating with vehicle electronics.

**Current release character:** Experimental, explicit about limitations, easy for early testers to understand. The active 0.1 family includes the Workshop UI, USB discovery/recovery and logging work through `0.1.4-usb-fix-dev`; actual scope and verification are in Section 2. The Ignition name does not imply that physical Android-to-ECU diagnostics works.

**Identity:** A dark technical palette, a single ignition pulse motif, and the message “It starts here.”

### Froodz VAG Diag 1.0 — Torque · First stable

**Tagline:** *Power means nothing without control.*<br>
**Why this name:** Torque describes rotational force and belongs at the heart of the powertrain. The first stable version should likewise be built on a dependable core rather than a long list of partly supported functions.

**Suggested release character:** Clearly defined supported adapters, ECUs, vehicles, and read operations; consistent diagnostic reports; dependable error recovery; and documentation that explains both successes and limitations. A 1.0 release should have a formal acceptance gate rather than being published simply because the version number reached 1.0.

**Identity:** A precision torque-wrench / drivetrain motif and the message “Built on measurable reliability.”

### Froodz VAG Diag 2.0 — Turbo · Second generation

**Tagline:** *More capability. Less friction.*<br>
**Why this name:** A turbocharger increases the air available to an engine by using exhaust energy. As a codename it suggests scaling the usefulness of a strong existing core, not sacrificing correctness for apparent speed.

**Suggested release character:** Better responsiveness, richer visualization, stronger data organization, expanded *verified* vehicle coverage, and smoother acquisition of supported measurements. Feature growth must remain bounded by adapter and vehicle capabilities.

**Identity:** A turbine-compressor graphic and the message “Engineered to accelerate insight.”

### Froodz VAG Diag 3.0 — Redline · Third generation

**Tagline:** *Precision where it matters most.*<br>
**Why this name:** Redline marks an engine-speed operating limit. For software, it symbolizes discipline around boundary conditions, system load, timing, data integrity, and reliable interpretation.

**Suggested release character:** Mature handling of complex modules and communications, stronger diagnostics under failure conditions, useful traces for engineering investigation, and clear guardrails around privileged operations. This is a metaphor for robust engineering—not a suggestion to run a vehicle at redline.

**Identity:** A minimalist tachometer arc and the message “Know the limits. Master the data.”

### Froodz VAG Diag 4.0 — Overdrive · Fourth generation

**Tagline:** *Go further with less effort.*<br>
**Why this name:** Overdrive commonly denotes a transmission ratio that reduces engine speed at a given road speed. As a mature-product metaphor it emphasizes efficient workflows, long-term maintainability, and a powerful experience that feels effortless.

**Suggested release character:** A cohesive, well-tested diagnostics platform with reliable updates, curated compatibility documentation, maintainable modules, polished reports, and accessible user workflows. “Overdrive” does **not** mean unbounded performance or universal vehicle support.

**Identity:** A clean high-speed drivetrain motif and the message “Engineering maturity in motion.”

---

## 4. Every release, explained in technical detail

The entries below preserve the **chosen order and wording**. Each one separates the actual automotive meaning from a **possible development focus**. A codename is an identity, not a guarantee that the named component or protocol is supported.

### 0.1 — Ignition · Alpha

- **Technical basis:** In a conventional spark-ignition engine, the ignition system provides high voltage to initiate a spark at the plug. Modern engine controllers coordinate this event using engine position and operating conditions.
- **Why it fits:** The first boot of a diagnostic application is analogous to turning the key: the system comes alive before it can do deeper work.
- **Possible engineering focus:** Core architecture, local configuration, adapter detection, clear connection states, basic logs, and a safe demonstration or simulated data mode.
- **Release-quality question:** Can a new tester install, launch, inspect the connection state, and exit without unintended vehicle changes?
- **Actual alignment:** The retained `0.1.0` artifacts, public `0.1.1`, local `0.1.2-workshop-dev`, `0.1.3-can-foundation-dev` and current `0.1.4-usb-fix-dev` all retain **Ignition**. Their capabilities and qualification differ; use the artifact ledger rather than the family name as evidence.

### 0.2 — Spark · Alpha

- **Technical basis:** A spark plug bridges a calibrated gap to ignite a combustible mixture in an appropriate engine. Spark quality depends on the ignition system, plug condition, and operating conditions.
- **Why it fits:** The first small, meaningful diagnostic result is the software equivalent of a visible spark.
- **Possible engineering focus:** A readable diagnostic event stream, a minimal supported ECU query, timestamps, unit handling, and useful explanations for a failed query.
- **Release-quality question:** Does the application distinguish a valid reading from stale, missing, unsupported, or simulated data?
- **Historical alignment:** Public `0.2.0-preview.1` maps to **Spark** as an experimental UI preview. Its publication does not establish the possible ECU-query focus above or make Spark a qualified diagnostic release. The current local workstream remains the explicitly identified Ignition build until a separate version decision is made.

### 0.3 — Piston · Alpha

- **Technical basis:** A piston moves through the cylinder, transferring combustion pressure through the connecting rod into crankshaft rotation. Its cyclic operation is a useful metaphor for repeatable processing.
- **Why it fits:** Diagnostic software must request, decode, validate, display, and repeat operations predictably.
- **Possible engineering focus:** Structured request cycles, retry boundaries, timeouts, cancellation, result history, and automated test fixtures for the same query over many runs.
- **Release-quality question:** Does repeated use leave the adapter and application in a known state?

### 0.4 — Crankshaft · Alpha

- **Technical basis:** The crankshaft converts reciprocating movement into rotation. Crankshaft position and rotational speed are critical references for engine management.
- **Why it fits:** Like the crankshaft, accurate timing coordinates many dependent actions.
- **Possible engineering focus:** Monotonic timestamps, RPM display if a verified data source exists, rate calculations, correct sampling metadata, and synchronized diagnostic logs.
- **Release-quality question:** Are timestamps, sample ordering, and measurements handled consistently despite delays or dropped messages?

### 0.5 — Lambda · Beta

- **Technical basis:** Lambda (λ) expresses actual air–fuel ratio relative to the stoichiometric ratio. Oxygen-sensor systems help engine management infer mixture behavior; narrowband and wideband sensors provide different information and should not be interpreted interchangeably.
- **Why it fits:** This is the transition from displaying raw numbers to interpreting them responsibly.
- **Possible engineering focus:** Measurement units, sensor-type awareness, reference explanations, trend charts, and cautionary interpretation rather than oversimplified “rich/lean” claims.
- **Release-quality question:** Does every interpreted value identify its source, units, conditions, and limitations?

### 0.6 — K-Line · Beta

- **Technical basis:** K-Line is a legacy serial automotive diagnostic communication line associated with ISO 9141 and ISO 14230/KWP2000 implementations. Older Volkswagen Group vehicles also use protocol families such as KW1281 and KWP2000.
- **Why it fits:** Strong diagnostics should recognize historical vehicles, not only current networking technology.
- **Possible engineering focus:** An explicit legacy-protocol architecture, adapter-capability checks, session/time-out handling, and device-specific compatibility testing where legal and feasible.
- **Release-quality question:** Are older vehicles described as *tested*, *experimental*, or *unsupported* instead of assuming all K-Line ECUs behave alike?

### 0.7 — Injector · Beta

- **Technical basis:** Fuel injectors meter fuel through precisely controlled opening events, with strategy varying by engine and injection system (for example port injection versus direct injection).
- **Why it fits:** Precision and repeatability matter more than indiscriminate command execution.
- **Possible engineering focus:** Typed diagnostic parameters, meaningful grouping of engine/fuel-system observations, exportable data, and strict separation between passive reading and active output tests.
- **Release-quality question:** Can a user clearly tell whether an operation only reads data or may actively command a vehicle component?

### 0.8 — CANbus · Beta

- **Technical basis:** Controller Area Network (CAN) is a vehicle network technology supporting message exchange among electronic modules. **CAN transport alone does not imply support for every diagnostic service, vehicle, or ECU.**
- **Why it fits:** The application grows from a single conversation into an organized view of multiple modules.
- **Possible engineering focus:** Adapter connection abstraction, CAN frame/transport inspection when supported, module discovery workflows, and clear separation of bus traffic from decoded diagnostic data.
- **Release-quality question:** Are transport-level successes distinguished from a correctly decoded ECU diagnostic response?

### 0.9 — Synchro · Release candidate

- **Technical basis:** Engine synchronization concerns crankshaft/camshaft phase alignment and coordinated timing. Synchronization problems can manifest as implausible or inconsistent engine signals.
- **Why it fits:** Before stability, different application subsystems must agree: UI, data acquisition, session state, exports, and recovery.
- **Possible engineering focus:** Release-candidate bug fixing, integration tests, data consistency, clean disconnects, regression coverage, and reproducible packaging.
- **Release-quality question:** Does the candidate repeatedly pass the documented stability and safety checklist on supported setups?

### 1.0 — Torque · First stable

- **Technical basis:** Torque is the turning moment of force, often measured in N·m. Power and torque are related through rotational speed but are not interchangeable.
- **Why it fits:** A first stable release should deliver trustworthy practical value rather than speculative feature breadth.
- **Possible engineering focus:** Stable read-only diagnostics on a published support matrix, disciplined fault reporting, documented installation and recovery, and regression-tested adapters.
- **Release-quality question:** Can the team explain exactly what works, on which hardware/vehicles, how to reproduce it, and what remains unsupported?

### 1.1 — Camshaft · Stable update

- **Technical basis:** A camshaft operates engine valves; camshaft position and variable valve timing may be monitored or controlled by the engine management system.
- **Why it fits:** This name represents a move from surface readings to relationships between components and operating states.
- **Possible engineering focus:** Better contextual grouping of supported engine data and explanations of timing-related signals where available. Do not infer mechanical faults from a single generic value.
- **Release-quality question:** Are complex values shown with correct definitions and clearly labeled diagnostic uncertainty?

### 1.2 — Knock · Stable update

- **Technical basis:** Combustion knock is unwanted auto-ignition that can generate pressure oscillations; sensors and engine control strategies respond to knock-related signals. Knock detection and its interpretation are engine-specific.
- **Why it fits:** Detection, warnings, and protective interpretation are central to dependable diagnostics.
- **Possible engineering focus:** Better DTC categorization, anomaly notifications for verified parameters, and transparent thresholds that avoid false diagnoses.
- **Release-quality question:** Does the product avoid treating an inferred warning as a confirmed mechanical failure?

### 1.3 — FreezeFrame · Stable update

- **Technical basis:** Freeze-frame information is a snapshot of relevant operating conditions stored in connection with certain diagnostic faults. Availability and content depend on the ECU, diagnostic standard, and fault.
- **Why it fits:** Understanding a failure means preserving what was happening when it occurred, not merely displaying an error code.
- **Possible engineering focus:** Present available fault snapshots with timestamps or event context when actually provided, provide clear missing-data notices, and export evidence before any potentially destructive operation.
- **Release-quality question:** Are recorded ECU data and application-generated context visibly distinguished?

### 1.4 — FuelTrim · Stable update

- **Technical basis:** Fuel trims represent engine-management corrections to fuel delivery, commonly discussed as short-term and long-term adjustments in applicable gasoline control systems. Availability and semantics vary by engine and operating mode.
- **Why it fits:** This version symbolizes moving beyond isolated measurements toward pattern recognition.
- **Possible engineering focus:** Time-based graphs, comparisons under documented conditions, unit-aware export, and explanations of why one fuel-trim reading cannot establish a definitive fault.
- **Release-quality question:** Does analysis separate evidence, likely causes, and required follow-up tests?

### 2.0 — Turbo · Second generation

- **Technical basis:** A turbocharger uses exhaust energy to drive a turbine connected to a compressor, increasing intake charge density under suitable conditions.
- **Why it fits:** The second major generation should expand useful capabilities while protecting the reliability built into Torque.
- **Possible engineering focus:** Modular data pipelines, more responsive live views, scalable diagnostics history, refined navigation, and broader *tested* support.
- **Release-quality question:** Are performance gains demonstrated by benchmarks without reducing data correctness or session stability?

### 2.1 — Spool · Stable update

- **Technical basis:** Turbo spool refers informally to the process of accelerating the turbocharger toward a speed where useful boost is produced. Transient behavior depends on load, exhaust energy, and control strategy.
- **Why it fits:** Responsiveness and feedback shape how fast the tool *feels* during active diagnosis.
- **Possible engineering focus:** Streaming performance, visual refresh discipline, buffering, dropped-sample indicators, and clear latency measurement.
- **Release-quality question:** Can the user distinguish a real engine transient from UI lag, adapter latency, or interpolation?

### 2.2 — Wastegate · Stable update

- **Technical basis:** A wastegate bypasses exhaust flow around a turbo turbine to help regulate boost. Depending on design, it can be controlled pneumatically or electronically through engine-management strategies.
- **Why it fits:** Intelligent systems need control boundaries, not just maximum output.
- **Possible engineering focus:** Visualization of requested versus actual values when available, configurable and safe chart limits, and control-system explanations that do not imply an ECU actuator is faulty from one trace.
- **Release-quality question:** Are comparison curves labeled correctly, synchronized, and recorded under appropriate conditions?

### 2.3 — DualClutch · Stable update

- **Technical basis:** Dual-clutch transmissions use two clutch paths to enable rapid gear changes. Volkswagen Group vehicles include DSG variants, but their hardware, mechatronics, service data, and supported diagnostics differ by model.
- **Why it fits:** A diagnostics suite for VAG should eventually understand the breadth of the powertrain, not simply engine data.
- **Possible engineering focus:** An extensible transmission diagnostics section, module-specific signal definitions, and tested read-only fault and data workflows on compatible gearboxes.
- **Release-quality question:** Are gearbox variants and data identifiers kept distinct rather than treated as universal DSG readings?

### 2.4 — LaunchControl · Stable update

- **Technical basis:** Launch control is a vehicle performance strategy that coordinates powertrain behavior under specific conditions. It is not a generic diagnostic protocol or a universally safe function.
- **Why it fits:** The software metaphor is a deliberate, guided start to a complex task.
- **Possible engineering focus:** Guided scan sessions, presets for *read-only* checks, preflight checks, progress indicators, and controlled report generation.
- **Release-quality question:** Are guided workflows clear, reversible where possible, and unable to initiate undocumented vehicle control actions?

### 3.0 — Redline · Third generation

- **Technical basis:** Engine redline denotes an upper RPM boundary specified by design and operating limitations. It varies across engines and should never be treated as a recommended test target.
- **Why it fits:** Complex diagnostic software must remain correct at its own limits: noisy data, intermittent connections, multiple ECUs, and unsupported features.
- **Possible engineering focus:** Improved architecture resilience, performance under load, advanced traceability, and strict operational safeguards.
- **Release-quality question:** Does stress testing include deliberate connection loss, malformed responses, cancellation, and recovery?

### 3.1 — Gateway · Stable update

- **Technical basis:** A gateway ECU helps route or mediate communications between different vehicle networks and modules. Vehicle topology and access policies vary considerably by platform.
- **Why it fits:** The release name represents a coherent vehicle-wide perspective.
- **Possible engineering focus:** ECU inventory views, topology-aware organization, connection lifecycle tracking, and explicit messages for access restrictions or unsupported gateway behavior.
- **Release-quality question:** Does module detection distinguish physically absent, not responding, unauthorized, and not yet supported?

### 3.2 — UDS · Stable update

- **Technical basis:** Unified Diagnostic Services (UDS) is the ISO 14229 family of diagnostic services used for communication with vehicle ECUs. It describes diagnostic service behavior; the underlying transport can vary. UDS by itself does not grant authorization to protected functions.
- **Why it fits:** A modern diagnostics platform benefits from a standards-aware approach and clear service semantics.
- **Possible engineering focus:** Better abstraction of diagnostic sessions and negative responses; safe, documented handling of supported read services; integration tests using simulated ECUs and verified real hardware.
- **Release-quality question:** Are service support, session/security constraints, and ECU-specific behavior verified instead of assumed?

### 3.3 — CAN-FD · Stable update

- **Technical basis:** CAN FD (CAN with Flexible Data-Rate) extends classic CAN with a larger data field and the option of a faster data phase. It requires compatible interfaces and ECUs; the presence of a CAN port does not establish CAN-FD capability.
- **Why it fits:** This name represents evolution in vehicle network technology and readiness to support it correctly.
- **Possible engineering focus:** Capability negotiation, adapter identification, transport-agnostic interfaces, and tested data-path handling for suitable hardware.
- **Release-quality question:** Is CAN-FD support declared only for adapters and vehicles actually validated for it?

### 4.0 — Overdrive · Fourth generation

- **Technical basis:** An overdrive ratio generally allows transmission output to rotate faster than the input, reducing engine RPM at a given road speed under appropriate gearing. It conveys sustainable efficiency rather than constant maximum load.
- **Why it fits:** A fourth major generation should reduce the effort needed to conduct trustworthy diagnostics and maintain the software over time.
- **Possible engineering focus:** Unified workflows, coherent cross-module reports, dependable update/recovery, polished accessibility, well-maintained diagnostics knowledge, and a rigorous support lifecycle.
- **Release-quality question:** Can a new user complete supported procedures reliably while a maintainer can reproduce, test, and fix faults without heroics?

---

## 5. Versioning and public naming convention

### 5.1 Canonical display pattern

Use:

```text
Froodz VAG Diag <version> <Codename> — <Stage>
```

Examples:

```text
Froodz VAG Diag 0.1 Ignition — Alpha
Froodz VAG Diag 0.1.4-usb-fix-dev Ignition — Alpha development build
Froodz VAG Diag 0.2.0-preview.1 Spark — Historical UI preview
Froodz VAG Diag 0.5 Lambda — Beta
Froodz VAG Diag 0.9 Synchro — Release Candidate
Froodz VAG Diag 1.0 Torque — Stable
Froodz VAG Diag 2.0 Turbo — Stable
Froodz VAG Diag 3.0 Redline — Stable
Froodz VAG Diag 4.0 Overdrive — Stable
```

### 5.2 Release tags and patch behavior

Recommended machine-readable convention:

The examples below are **future tag-format illustrations**, not a rewrite of existing tags. The verified published release tags at this snapshot are `v0.1.1` and `v0.2.0-preview.1`; both are GitHub prereleases. This is not an inventory of every Git ref. Local suffixes such as `-workshop-dev`, `-can-foundation-dev` and `-usb-fix-dev` remain part of their exact recorded build labels. No local build receives a public tag through this documentation update.

| Human label | Example Git tag | Meaning |
|---|---|---|
| `0.1 Ignition — Alpha` | `v0.1.0-alpha.1` | First proposed alpha build in Ignition family |
| `0.5 Lambda — Beta` | `v0.5.0-beta.1` | First proposed beta build in Lambda family |
| `0.9 Synchro — RC` | `v0.9.0-rc.1` | First proposed release candidate in Synchro family |
| `1.0 Torque — Stable` | `v1.0.0` | Proposed first stable major release |
| `1.0 Torque — Patch` | `v1.0.1` | Fix to the same codename, not a new name |
| `2.3 DualClutch — Stable` | `v2.3.0` | Minor release in the Turbo generation |
| `4.0 Overdrive — Stable` | `v4.0.0` | Proposed fourth major generation |

**Rule:** A patch keeps its parent codename. For instance, `1.0.4` remains **Torque**; do not invent a new codename for routine hotfixes.

**Applied now:** every `0.1.x` build is **Ignition**; `0.2.0-preview.1` is **Spark**. Treat the complete major/minor family as the lookup key, not the final digit or a matching feature word. A future 0.1 patch stays Ignition; a future 0.2 build uses Spark. The next version/tag is not assigned here.

**Rule:** Chosen roadmap numbers are project identifiers. Applying strict Semantic Versioning is a separate project policy decision; if SemVer is adopted, assess API compatibility and deprecation requirements independently.

**Rule:** If a milestone is skipped, delayed, or renamed by an explicit project decision, keep an auditable changelog. Never silently recycle a published codename to identify unrelated binaries.

### 5.3 Slugs and filenames

Keep visible names styled for readers, but use stable lowercase ASCII slugs for machine identifiers.

| Display codename | Slug |
|---|---|
| K-Line | `k-line` |
| CANbus | `canbus` |
| FreezeFrame | `freezeframe` |
| FuelTrim | `fueltrim` |
| DualClutch | `dualclutch` |
| LaunchControl | `launchcontrol` |
| CAN-FD | `can-fd` |

Example **illustrative** package naming:

```text
froodz-vag-diag-v1.0.0-torque-win-x64.zip
froodz-vag-diag-v1.0.0-torque-linux-x86_64.tar.gz
froodz-vag-diag-v1.0.0-torque-checksums.txt
```

The package targets are only examples; they do **not** assert current Windows/Linux availability. Real assets should include actual platform, architecture, hash, and signature details.

Current deliverables are the Android APKs in Section 2, not those illustrative desktop packages. Keep their already-delivered filenames unchanged. A future asset may include the codename only when it is actually built, verified and named that way; do not imply an old checksum applies to a rebuilt or repackaged file.

## 6. Release identity, scope, and safety rules

### 6.1 Identity is not a feature claim

**Critical distinction:** A codename such as **K-Line**, **CANbus**, **UDS**, or **CAN-FD** does not establish that the application can use that protocol. Likewise, **Injector**, **Wastegate**, **DualClutch**, and **LaunchControl** do not establish actuator controls, adaptations, tuning, transmission coding, or launch-control activation. A name can serve as a theme even when the associated feature is not part of that release.

Keep naming, build existence, publication and physical support separate in public materials:

- **Selected codename:** Approved identity attached to a version family; includes planned milestones and the actual family mappings in Section 2.
- **Development milestone:** An engineering objective with tracked tests; not necessarily finished.
- **Built / locally delivered:** A retained artifact with exact manifest identity, hash and local evidence; not necessarily published or physically qualified.
- **Published prerelease:** An actual retrievable GitHub release and assets. At this snapshot these are `v0.1.1` and `v0.2.0-preview.1`; neither is a stable support claim.
- **Supported / physically qualified:** An explicitly tested adapter/firmware/phone/vehicle/ECU operation described in a support matrix. Local tests or public prerelease status do not establish this.

### 6.2 Diagnostic safety principles

1. **Read-first workflows.** Clearly separate passive observations from operations that can change ECU state, perform adaptation/coding, trigger an actuator, erase DTCs, or flash software.
2. **No implied authorization.** Access to a car's diagnostic port does not automatically authorize changing protected systems. Respect vehicle-owner consent, applicable laws, and manufacturer security controls.
3. **No silent destructive operations.** Erasing DTCs can remove valuable fault context. Require explicit confirmation and encourage saving reports and available freeze-frame data first.
4. **Stable-state checks.** Display vehicle or adapter preconditions when relevant; never imply engine-running, road-test, or moving-vehicle procedures are safe in every context.
5. **Adapter and ECU specificity.** Name and test exact adapter models/firmware, ECU families, vehicle platforms, protocols, and supported commands. Avoid universal claims.
6. **Report integrity.** Record capture time, units, source, module identification, tool version, and whether data is live, cached, imported, or simulated.
7. **Graceful failure.** Timeouts, malformed messages, dropped connections, and unsupported ECUs must fail predictably and clearly.

### 6.3 Suggested visual language

| Generation | Visual motif | Short phrase |
|---|---|---|
| **Ignition generation** `0.x` · individual 0.1–0.9 codenames remain distinct | Ignition pulse / spark / rotating shaft | *Bring the system to life* |
| **Torque** `1.x` | Precision mechanics / dynamometer line | *Measure with confidence* |
| **Turbo** `2.x` | Compressor wheel / smooth flow | *Accelerate insight* |
| **Redline** `3.x` | Tachometer arc / system limits | *Precision under pressure* |
| **Overdrive** `4.x` | Gear train / efficient movement | *Power without friction* |

These are design directions only and do not require changing the product logo with every update.

## 7. Suggested release engineering checklist

Every actual release should include a short, evidence-based checklist rather than claiming a name implies completion.

- [ ] Confirm the version number and exact codename against the table above.
- [ ] Identify the release channel: alpha, beta, RC, stable, or patch.
- [ ] Document new features, fixes, known issues, and deliberate exclusions.
- [ ] Identify tested operating systems, CPU architectures, adapters, interface firmware, and vehicle/ECU combinations.
- [ ] Run tests for disconnected/unsupported devices, timeouts, aborts, and malformed responses.
- [ ] Review data decoding, units, timestamp accuracy, and exported-report contents.
- [ ] Test any write-capable operation separately with explicit authorization and safeguards.
- [ ] Build clean release assets and publish checksums/signatures where available.
- [ ] Include licensing, notices, changelog, documentation, and recovery instructions.
- [ ] Mark the release as **published** only after actual artifacts and notes are available.

### Template for a future GitHub Release

```markdown
# Froodz VAG Diag <version> <Codename> — <Stage>

> Release date: <YYYY-MM-DD>
> Status: <Alpha / Beta / RC / Stable>

## Highlights
- <A verified feature or improvement>

## Fixes
- <A confirmed bug fix>

## Tested environments
- Application platform: <OS / architecture>
- Interface: <adapter + firmware>
- Vehicle / ECU: <verified examples>
- Protocols / operations: <verified only>

## Known limitations
- <What remains unsupported or experimental>

## Safety and data handling
- <Read-only versus active operations; required precautions>

## Assets and verification
- <Artifact links, hashes, and optional signatures>
```

## 8. Complete release tree

```text
Froodz VAG Diag
|
+-- 0.x  IGNITION GENERATION  [experimental -> release candidate]
|   +-- 0.1  Ignition       [Active alpha family; current local 0.1.4-usb-fix-dev]
|   |   +-- 0.1.0         [Legacy bench / KW1281 APKs; distinct hashes, reused code 1]
|   |   +-- 0.1.1         [Published experimental GitHub prerelease]
|   |   +-- 0.1.2-workshop-dev       [Historical local snapshot]
|   |   +-- 0.1.3-can-foundation-dev [Frozen local build]
|   |   +-- 0.1.4-usb-fix-dev       [Current frozen local build; not published]
|   +-- 0.2  Spark          [Alpha identity; historical public 0.2.0-preview.1 UI preview]
|   +-- 0.3  Piston         [Planned Alpha]
|   +-- 0.4  Crankshaft     [Planned Alpha]
|   +-- 0.5  Lambda         [Planned Beta]
|   +-- 0.6  K-Line         [Planned Beta]
|   +-- 0.7  Injector       [Planned Beta]
|   +-- 0.8  CANbus         [Planned Beta]
|   +-- 0.9  Synchro        [Planned Release Candidate]
|
+-- 1.x  TORQUE GENERATION    [planned first stable family]
|   +-- 1.0  Torque         [First Stable]
|   +-- 1.1  Camshaft       [Stable Update]
|   +-- 1.2  Knock          [Stable Update]
|   +-- 1.3  FreezeFrame    [Stable Update]
|   +-- 1.4  FuelTrim       [Stable Update]
|
+-- 2.x  TURBO GENERATION     [planned second generation]
|   +-- 2.0  Turbo          [Major]
|   +-- 2.1  Spool          [Stable Update]
|   +-- 2.2  Wastegate      [Stable Update]
|   +-- 2.3  DualClutch     [Stable Update]
|   +-- 2.4  LaunchControl  [Stable Update]
|
+-- 3.x  REDLINE GENERATION   [planned third generation]
|   +-- 3.0  Redline        [Major]
|   +-- 3.1  Gateway        [Stable Update]
|   +-- 3.2  UDS            [Stable Update]
|   +-- 3.3  CAN-FD         [Stable Update]
|
+-- 4.x  OVERDRIVE GENERATION [planned fourth generation]
    +-- 4.0  Overdrive      [Major]
```

## 9. Terminology and technical references

This naming scheme draws on actual automotive engineering. The references below are for technical context only; they do not establish that Froodz VAG Diag currently implements the listed technologies.

- **ISO 14229 / UDS:** Kvaser, [Unified Diagnostic Services](https://kvaser.com/about-can/can-standards/unified-diagnostic-services/).
- **CAN and CAN FD transport:** [Diagnostics over CAN — py-uds documentation](https://uds.readthedocs.io/en/v5.0.0/pages/knowledge_base/can.html).
- **ECU diagnostic data modeling:** ASAM, [MCD-2 D / ODX](https://www.asam.net/standards/detail/mcd-2-d/).
- **VAG legacy protocol context:** SAE, [TP1.6 Vehicle Diagnostic Protocol](https://saemobilus.sae.org/standards/j3054_202205-tp16-vehicle-diagnostic-protocol).

## 10. Naming decision record

**Selected for the Froodz VAG Diag roadmap:** all 24 release codenames and version assignments listed in Section 2, covering **0.1 Ignition** through **4.0 Overdrive**.

**Applied to actual artifacts:** Section 2.1 is the verified alignment, including the historical Spark UI preview and current local Ignition build. Local APK/source manifests, frozen receipts and read-only GitHub release/asset verification back this snapshot. Older same-version APKs remain distinct by hash; none was silently renumbered.

**Publication scope:** the original naming-only plan is now connected to real artifact/publication status. All 24 chosen names and family assignments are preserved. The owner subsequently approved updating GitHub documentation, branding and existing release display names. No APK rebuild, versionCode change, binary/asset rename, new Git tag or binary upload is included in this naming publication; the local 0.1.3/0.1.4 APKs remain locally delivered builds, not new public releases.

**Not decided by this document:** future release dates, final engineering scope, platform support, adapter support, ECU compatibility, trademarks, distribution rights, future publication status, deployment policy, or the authorization to modify or publish any remote repository. Known historical dates/statuses in Section 2 are recorded evidence, not new release decisions.

**Canonical documentation location:** `docs/RELEASE_CODENAMES.md`, linked from the repository README. The matching Downloads copy keeps its original filename. Publishing this policy does not publish or physically qualify a local APK. The repository URL and legacy source paths remain stable technical identifiers; public-facing product naming is **Froodz VAG Diag**.

---

*Froodz VAG Diag — Diagnose with precision. Evolve with performance.*
