<div align="center"><img width="512" height="153" alt="CREATE_DIESEL_GENERATORS" src="https://github.com/user-attachments/assets/ca4378d3-9bd5-45bf-9629-fd5a28170767" /></div>
<hr>
<div align="center">
<a href="https://modrinth.com/mod/create-diesel-generators"><img src="https://img.shields.io/modrinth/dt/create-diesel-generators?logo=modrinth&label=&suffix=%20&style=flat&color=242629&labelColor=5ca424&logoColor=1c1c1c"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/create-diesel-generators"><img src="https://cf.way2muchnoise.eu/869316.svg"></a>
<a href="https://discord.gg/pUgaSXcGEQ"><img src="https://img.shields.io/discord/1121792423836799128?color=5865f2&label=Discord"></a><br>
</div>
Adding Diesel Generators and industrial-like features to the <a href="https://www.curseforge.com/minecraft/mc-mods/create">Create</a> mod.

### Port 1.20.1

This branch ports **1.3.12** to **Minecraft 1.20.1 / Forge 47.1.30 / Create 0.5.1.j / Java 17**. The supplied Create, Flywheel 0.6.11-13 and Registrate jars are kept in `libs/`; Ponder is provided by Create itself.

```bash
./gradlew --no-daemon clean build
```

The [port status and verification guide](PORT_STATUS.md) documents the implemented fixes, target-specific recipe differences, networking protocol 4, and the isolated GameTest/client smoke suites. GitHub Actions builds the reobfuscated mod, verifies its packaged resources/refmap, runs all required GameTests and launches a real software-GL client world. The JAR and verification logs are available in the workflow artifacts.

Both client and server must use this port revision. Its tests are not included in the distribution JAR. `-PportTests runClient` is an **opt-in test harness**, not the normal play configuration; use a clean `run-client-smoke/` test directory when repeating it.

### Overview
The main focus of this mod is to provide a streamlined implementation of industrial-like features to the create mod while maintaining its style.

While this is not gonna give you the absolute most realistic implementation, this provides something simple. You don't have to overthink anything when playing. Just play.

If you want to contribute, feel free to open a PR (to the default branch), whether to fix something or add a feature.

If you want to help translate this mod, make a pull request with the translation files<br>
