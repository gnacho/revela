---
type: planning
entity: phase
plan: "revela"
phase: 0
status: pending
created: "2026-10-12"
updated: "2026-10-12"
---

# Phase 0: Toolchain + esqueleto

> Part of [revela](../plan.md)

## Objective

Entorno Android funcional en esta maquina (sin root) y esqueleto de app Compose que abre una imagen con zoom/pan, compilada e instalada en el movil del usuario.

## Scope

### Includes

- Instalacion de Android SDK cmdline-tools en ~/Android/Sdk (descarga oficial, sin pacman).
- sdkmanager: licencias aceptadas + platform-tools, platforms;android-XX y build-tools (XX decidido tras ver la version de Android del movil via adb).
- Esqueleto Gradle Kotlin DSL: modulo app, Compose, minSdk 29, applicationId app.revela, .gitignore Android.
- Pantalla unica de editor minima: abrir imagen (Photo Picker o intent provisional), mostrarla con zoom (pinch) y pan.
- Build debug verificable en local (./gradlew assembleDebug).

### Excludes

- Cualquier ajuste/edicion real, export, healing, presets.
- Android Studio (IDE grafico) como requisito; opcional para el usuario.
- CI, releases firmadas.

## Prerequisites

- [x] Java 17 presente (verificado 2026-10-12).
- [ ] Movil del usuario con depuracion USB conectado para el gate final (el usuario lo conecta y avisa).

## Deliverables

- [ ] SDK Android operativo en ~/Android/Sdk (sdkmanager/adb funcionan).
- [ ] Repo revela-git con proyecto Compose que compila: ./gradlew assembleDebug exit 0.
- [ ] APK debug instalado en el movil mostrando una imagen con zoom/pan fluido.

## Acceptance Criteria

- [ ] ./gradlew assembleDebug termina exit 0 en esta maquina.
- [ ] adb install -r del APK tiene exit 0 y la app arranca en el movil.
- [ ] El usuario confirma zoom/pan usable sobre una foto real ("listo").
- [ ] Version de Android del movil registrada en este plan para fijar targetSdk.

## Dependencies on Other Phases

Ninguna (fase inicial).

## Notes

- La ruta del wrapper: usar Gradle binario descargado una vez (zip oficial) para generar el wrapper; NO instalar gradle de sistema.
- Sin emulador: las verificaciones de dispositivo son via adb contra el movil fisico.
- Protocolo del usuario para hardware: el conecta el movil y avisa "listo"; nunca asumir que esta conectado.
