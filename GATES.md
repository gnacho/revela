# Gates: fase 5 revela - panel Herramientas overlay + animación + renombrados

OWNS: app/src/main/java/app/revela/MainActivity.kt, app/src/main/java/app/revela/render/ThumbRender.kt, plans/revela/todo.md

Scope: la rejilla de herramientas se abre como panel animado (slide-up ~250 ms) que NO oculta la foto (scrim + foto visible arriba), con etiquetas ES de Snapseed, verificado en emulador con build release.

- [x] G1: build y suite unitaria verdes tras el cambio
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q; echo "EXIT=$?"
  EXPECT: EXIT=0
  EVIDENCE: automatic-evidence=v1; definition-sha256=5d13c1326edfe72dd5c216a33589dd6767e96e858d2e58206cdee6d071c1abb6; exit=0; EXPECT=matched; output-sha256=418a5c17f33c70e99b0cc0a07fce69191489cfedc94164bfa903785777c5bd4b; output-bytes=7; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G2: etiquetas Quitamanchas y Ajustar imagen presentes en la UI
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && grep -q "Quitamanchas" app/src/main/java/app/revela/MainActivity.kt && grep -q "Ajustar imagen" app/src/main/java/app/revela/MainActivity.kt && echo LABELS_OK
  EXPECT: LABELS_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=737ac3a4f5bd8e11cb0fd9c67b0bb1446f6a57d72d53882cf7d5e5ed1a8a0794; exit=0; EXPECT=matched; output-sha256=f17c14533d2bee730fefd1815b60e39584fe60b3546192f8e84db9d9d6cbeaee; output-bytes=10; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G3: con el panel abierto la foto sigue visible (zona central superior con luminancia > 40/255 en release)
  CHECK: export PATH=$HOME/Android/Sdk/platform-tools:$PATH && cd /home/nacho/Documentos/Repos/revela-git && adb -s emulator-5554 uninstall app.revela > /dev/null 2>&1; adb -s emulator-5554 install app/build/outputs/apk/release/app-release.apk > /dev/null 2>&1 && adb -s emulator-5554 shell am start -n app.revela/.MainActivity > /dev/null 2>&1 && sleep 3 && adb -s emulator-5554 shell input tap 540 1227 && sleep 3 && adb -s emulator-5554 shell input tap 179 1405 && sleep 4 && adb -s emulator-5554 shell input tap 540 2264 && sleep 2 && adb -s emulator-5554 shell screencap -p /data/local/tmp/g3.png > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g3.png /tmp/opencode/g3.png > /dev/null 2>&1 && magick /tmp/opencode/g3.png -crop 280x740+400+250 +repage -colorspace Gray -format "%[fx:mean*255]" info: > /tmp/opencode/g3lum.txt && python3 -c "v=float(open('/tmp/opencode/g3lum.txt').read().strip()); assert 40<=v<=255, v" && echo PHOTO_VISIBLE
  EXPECT: PHOTO_VISIBLE
  EVIDENCE: automatic-evidence=v1; definition-sha256=a0bfe4951689987ceaeb8cda1407b1695a5964a361df44028e4e97df6ec9bf35; exit=0; EXPECT=matched; output-sha256=7ad0d409cab0e4e1dcc84db8e7ddd31b559965396b9aa6ca894501fae3ce8285; output-bytes=14; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G4: apertura del panel con frame intermedio (slide-up real, no aparición en seco)
  CHECK: export PATH=$HOME/Android/Sdk/platform-tools:$PATH && cd /home/nacho/Documentos/Repos/revela-git && adb -s emulator-5554 uninstall app.revela > /dev/null 2>&1; adb -s emulator-5554 install app/build/outputs/apk/release/app-release.apk > /dev/null 2>&1 && adb -s emulator-5554 shell am start -n app.revela/.MainActivity > /dev/null 2>&1 && sleep 3 && adb -s emulator-5554 shell input tap 540 1227 && sleep 3 && adb -s emulator-5554 shell input tap 179 1405 && sleep 4 && adb -s emulator-5554 shell input tap 540 2264 && sleep 1 && adb -s emulator-5554 shell input tap 540 600 && sleep 1 && adb -s emulator-5554 shell "nohup screenrecord --time-limit 3 --bit-rate 8000000 /data/local/tmp/g4.mp4 > /dev/null 2>&1 &" > /dev/null 2>&1 && sleep 1 && adb -s emulator-5554 shell input tap 540 2264 && sleep 3 && adb -s emulator-5554 pull /data/local/tmp/g4.mp4 /tmp/opencode/g4.mp4 > /dev/null 2>&1 && rm -rf /tmp/opencode/g4f && mkdir -p /tmp/opencode/g4f && ffmpeg -loglevel error -i /tmp/opencode/g4.mp4 -vf fps=10 /tmp/opencode/g4f/f%02d.png > /dev/null 2>&1 && python3 /tmp/opencode/g4check.py > /tmp/opencode/g4out.txt 2>&1; tail -1 /tmp/opencode/g4out.txt; grep -q ANIM_OK /tmp/opencode/g4out.txt && echo ANIM_GATE_OK
  EXPECT: ANIM_GATE_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=c62f448ea468ef7909ad501498fc4f247b0a336e878ce5d311d793f4f04f58ef; exit=0; EXPECT=matched; output-sha256=82f598f91a52f5a3e217ee370b46776273bb1f7e1c6724cf19b9c76632c1e52b; output-bytes=21; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G5: release instalado arranca en primer plano sin crash
  CHECK: export PATH=$HOME/Android/Sdk/platform-tools:$PATH && adb -s emulator-5554 logcat -c && adb -s emulator-5554 shell am force-stop app.revela && adb -s emulator-5554 shell am start -n app.revela/.MainActivity > /dev/null && sleep 4 && adb -s emulator-5554 shell dumpsys window | grep mCurrentFocus | grep -q app.revela && echo FOCUS_OK
  EXPECT: FOCUS_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=ec0f91d01d820f2053a28896cd9a56d20f62e759b251e5a909e97889fe58e802; exit=0; EXPECT=matched; output-sha256=c3ac7ac24d96916d9132caeccb6020ccca31d8adb6a303d87075c5ad63a51489; output-bytes=9; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G6: tests unitarios verdes incluyendo AutoLevels (auto-niveles)
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ls app/src/test/java/app/revela/core/AutoLevelsTest.kt > /dev/null && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && echo TESTS_OK
  EXPECT: TESTS_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=918e48fb71f3ef1ecc760606f965d59287c3d5d8f4cfc6aedf393e75583210ab; exit=0; EXPECT=matched; output-sha256=248df82524633f6943bc2136126941223c3efee4cea57b1b749c1b64f1230789; output-bytes=9; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G7: el boton AUTO aplica ajustes visibles y snackbar en el emulador (release)
  CHECK: export PATH=$HOME/Android/Sdk/platform-tools:$PATH && cd /home/nacho/Documentos/Repos/revela-git && adb -s emulator-5554 uninstall app.revela > /dev/null 2>&1; adb -s emulator-5554 install app/build/outputs/apk/release/app-release.apk > /dev/null 2>&1 && adb -s emulator-5554 shell am start -n app.revela/.MainActivity > /dev/null 2>&1 && sleep 3 && adb -s emulator-5554 shell input tap 540 1227 && sleep 3 && adb -s emulator-5554 shell input tap 179 1405 && sleep 4 && adb -s emulator-5554 shell input tap 540 2264 && sleep 1 && adb -s emulator-5554 shell input tap 196 1667 && sleep 1 && adb -s emulator-5554 shell screencap -p /data/local/tmp/g7a.png > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g7a.png /tmp/opencode/g7a.png > /dev/null 2>&1 && adb -s emulator-5554 shell uiautomator dump /data/local/tmp/g7.xml > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g7.xml /tmp/opencode/g7.xml > /dev/null 2>&1 && TAP=$(python3 /tmp/opencode/g7tap.py) && adb -s emulator-5554 shell input tap $TAP && sleep 2 && adb -s emulator-5554 shell screencap -p /data/local/tmp/g7b.png > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g7b.png /tmp/opencode/g7b.png > /dev/null 2>&1 && adb -s emulator-5554 shell uiautomator dump /data/local/tmp/g7b.xml > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g7b.xml /tmp/opencode/g7after.xml > /dev/null 2>&1 && python3 /tmp/opencode/g7cmp.py > /tmp/opencode/g7out.txt 2>&1; tail -1 /tmp/opencode/g7out.txt; grep -q AUTO_OK /tmp/opencode/g7out.txt && echo AUTO_GATE_OK
  EXPECT: AUTO_GATE_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=9079fc130f8f6165b27a80be6b25fda4fb0331ff326d8e6ea9f4da759246cf97; exit=0; EXPECT=matched; output-sha256=07fbe60ec5036cf8aa450bd513f86d91d41930a81d767eff386bf9693a2cc90f; output-bytes=21; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

Handoff (fuera de los gates): el bug de iconos del OnePlus quedó resuelto el 18-Oct (la build actual los renderiza bien en el dispositivo, captura verificada); pendiente solo la confirmación visual del usuario.
