# Component boundaries and hardware safety

## Windows

VCDS remains Ross-Tech's proprietary Windows application. The related [VCDS-ARM64](https://github.com/Fredy-E/VCDS-ARM64) repository documents licensed installations and genuine interfaces. Running an x86 application under Windows ARM emulation does not make its binary native ARM64.

## Capture driver

[USBPcap-ARM64](https://github.com/Fredy-E/USBPcap-ARM64) captures Windows USB traffic. Its native ARM64 capture filter is not a Ross-Tech interface driver, an Android driver, or an interface firmware replacement. Driver trust, installation, runtime safety and hardware qualification remain distinct from build success.

## Android

VagDiag uses Android's USB-host APIs directly. It does not execute VCDS, load a Windows driver, unlock an interface, or implement ECU diagnostics. The USB target is the observed legacy HEX-USB VID/PID, not a claim of compatibility with every Ross-Tech product or cable.

**Identify** receives USB data without a bulk probe. A matching token/banner can be copied or spoofed and cannot prove authenticity. USB connection/claiming itself is not a zero-interaction operation.

**Init probe** changes USB control settings and sends observed bench traffic. Exact command meanings are not fully decoded. A confirmation dialog requires the user to acknowledge that the cable is disconnected from the vehicle's OBD port. This is an advisory guard, not an electrically enforced lockout. Do not use active probing on a vehicle.

No DTC-read, fault-clear, coding, adaptation or programming command is implemented. Do not infer vehicle-level behavior from a byte-framing test or a Windows-side capture.

Windows Subsystem for Android without USB passthrough cannot establish physical USB-OTG operation. A development APK or a green JVM test suite is not a production safety qualification.
