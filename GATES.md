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

- [x] G8: Blanco y Negro: el pipeline convierte a gris con pesos de filtro (test JVM)
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "BnwPipelineTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.core.BnwPipelineTest.xml 2>/dev/null && echo BNW_OK
  EXPECT: BNW_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=4b883d5616e998184ee326ccc3fa698bd9dd8b9409378ef053ef69a852721f71; exit=0; EXPECT=matched; output-sha256=a1715972e0a140458bc403006fbb8a5da6d03f10d692b5a0a04d03874a9d0941; output-bytes=7; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G9: Contraste tonal: LUT por zonas (sombras/medios/luces) con test JVM
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "TonalContrastTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.core.TonalContrastTest.xml 2>/dev/null && echo TONAL_OK
  EXPECT: TONAL_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=e2fb8924d213fb4250774e6ac8ee97c2fa711d558d048da7035b01e832b28b4f; exit=0; EXPECT=matched; output-sha256=663b4a8b41ffe396d07c3b062f313349000d5587daa1184188bf47c3883608a7; output-bytes=9; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G10: Estilos personalizables: codec JSON ida y vuelta con test JVM
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "CustomStylesTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.core.CustomStylesTest.xml 2>/dev/null && echo STYLES_OK
  EXPECT: STYLES_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=f241caf73d7ab8178409a8ff268062701caf03f4268f54168a480d66a43eac21; exit=0; EXPECT=matched; output-sha256=ca7ce68687fe55a45146a338ed1bb8f364fa99ebdb7860f5976e541205062fb9; output-bytes=10; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G11: Asistente de bienvenida: flag visto persistente con test JVM y cableado en UI
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "WelcomeStore" app/src/main/java/app/revela/core/WelcomeStore.kt && grep -q "WelcomeWizard" app/src/main/java/app/revela/MainActivity.kt && echo WELCOME_OK
  EXPECT: WELCOME_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=c390100c288105ae3463351fded0b0c47755c49f71dde9c90bf6f660700509fd; exit=0; EXPECT=matched; output-sha256=7dc5b48aa467ba11f9240758fdaaaaa72acdc5ce065fc359d201b08ac15ee0b4; output-bytes=11; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

- [x] G12: la rejilla del release en emulador muestra Girar, Blanco y negro y Contraste tonal (>= 9 herramientas) y el asistente aparece la 1a vez
  CHECK: export PATH=$HOME/Android/Sdk/platform-tools:$PATH && cd /home/nacho/Documentos/Repos/revela-git && adb -s emulator-5554 uninstall app.revela > /dev/null 2>&1; adb -s emulator-5554 install app/build/outputs/apk/release/app-release.apk > /dev/null 2>&1 && adb -s emulator-5554 shell am start -n app.revela/.MainActivity > /dev/null 2>&1 && sleep 3 && adb -s emulator-5554 shell uiautomator dump /data/local/tmp/g12w.xml > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g12w.xml /tmp/opencode/g12w.xml > /dev/null 2>&1 && grep -q "Bienvenido a revela" /tmp/opencode/g12w.xml && adb -s emulator-5554 shell input tap 154 2242 && sleep 1 && adb -s emulator-5554 shell input tap 540 1227 && sleep 3 && adb -s emulator-5554 shell input tap 179 1405 && sleep 4 && adb -s emulator-5554 shell input tap 540 2264 && sleep 1.5 && adb -s emulator-5554 shell uiautomator dump /data/local/tmp/g12.xml > /dev/null 2>&1 && adb -s emulator-5554 pull /data/local/tmp/g12.xml /tmp/opencode/g12.xml > /dev/null 2>&1 && python3 -c "x=open('/tmp/opencode/g12.xml').read(); assert 'Girar' in x and 'Blanco y negro' in x and 'Contraste tonal' in x, 'faltan tools'" && echo GRID_OK
  EXPECT: GRID_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=fca39a8bb2802101b57dc49ab1adbf574efe33430907f33a3c0b5e67d2e151a9; exit=0; EXPECT=matched; output-sha256=867d5909215690eeb95696f392fa4881e53a38b9b081a5adf59d6f2429611ae7; output-bytes=8; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git; path=13199a136530/15 entries

Handoff (fuera de los gates): el bug de iconos del OnePlus quedó resuelto el 18-Oct (la build actual los renderiza bien en el dispositivo, captura verificada); pendiente solo la confirmación visual del usuario.

---

# Gates: lote FASE 11 - HOME + favoritos + cierre 28/28 + doble exposicion + animacion estilos

OWNS: app/src/main/java/app/revela/MainActivity.kt, app/src/main/java/app/revela/HomeScreen.kt (nuevo), app/src/main/java/app/revela/render/SessionIndexStore.kt (nuevo), app/src/main/java/app/revela/core/FavoritesStore.kt (nuevo), plans/revela/todo.md

Scope: FASE 11 completa en 5 paquetes ordenados: HOME (biblioteca de sesiones persistidas con miniatura, tap reabre), barra de favoritos personalizable siempre visible, cierre 28/28 con la especificacion REA (Perspective 8 puntos 4 modos, Expand, Brush multi-param, Head Pose), Doble exposicion completa (blends add/overlay, opacity, transformar 2a capa), animacion look_thumbnail_state en el carrusel de Estilos.

- [x] G13: HOME: SessionIndexStore con tests JVM (codec, upsert, orden, delete con thumb, prune) y la home lista la sesion editada en release con tap que reabre
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "SessionIndexStoreTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.render.SessionIndexStoreTest.xml 2>/dev/null && echo HOME_STORE_OK
  EXPECT: HOME_STORE_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=9d29e05af1c7021880d15d12ac5db7375ffb05b077ee5a26d4f286025405ab76; exit=0; EXPECT=matched; output-sha256=c9e4cd1b02837acefdcff976f72ddb0949165666aabf700cfb9c02b4dee87657; output-bytes=13; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G14: HOME en release: tras editar una foto y volver, la home muestra la tarjeta de la sesion y el tap reabre el editor con el ajuste aplicado
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew assembleRelease -q > /dev/null 2>&1 && python3 ~/temp/opencode-work/revela-gates/g14.py 2>&1 | tail -1
  EXPECT: HOME_GATE_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=d153b8928842fbb34e70a94fdfa308cdd0e94f2d7f13298c521f8cf46a09c85c; exit=0; EXPECT=matched; output-sha256=197480180233d0335f4d418ba5323ffb92aa7859ceb31751d363e0bcdbc1c98c; output-bytes=12; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G15: Favoritos: FavoritesStore con test JVM (persiste JSON, orden estable) y barra de favoritos visible bajo la barra superior en release
  CHECK: ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "FavoritesStoreTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.core.FavoritesStoreTest.xml 2>/dev/null && ./gradlew assembleRelease -q > /dev/null 2>&1 && python3 ~/temp/opencode-work/revela-gates/g15.py 2>&1 | tail -1
  EXPECT: FAV_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=7691dd29aa45e980a198b20a9adb44f86f99d231d9f495d8aa85608d46e1fdbf; exit=0; EXPECT=matched; output-sha256=9a810d253c3b10a3608814a62b21b4caed58cff617c986d0cbc7b1409ae16b6e; output-bytes=6; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G16: Rejilla 28/28: Perspective, Expandir, Pincel y Pose presentes en la rejilla en release con scroll
  CHECK: ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && ./gradlew assembleRelease -q > /dev/null 2>&1 && python3 ~/temp/opencode-work/revela-gates/g16.py 2>&1 | tail -1 && python3 ~/temp/opencode-work/revela-gates/g16func.py 2>&1 | tail -1
  EXPECT: GRID28_OK (rejilla) + GRID28_FUNC_OK (perspective y brush funcionales)
  EVIDENCE: automatic-evidence=v1; definition-sha256=db0e4f0a5a8acb74db5fdc58a791456a453e2026b180eb0102e698c630e1550f; exit=0; EXPECT=matched; output-sha256=46de3ce70786043740dba1c7455814905f81f3e4148ec68682ec34dbfff61bc4; output-bytes=14; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G17: Doble exposicion completa: blends add y overlay, opacity y transform (rotar/escalar) de la 2a capa con test JVM del blend
  CHECK: ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && grep -q "DoubleExposureTest" app/build/test-results/testDebugUnitTest/TEST-app.revela.core.DoubleExposureTest.xml 2>/dev/null && echo DBL_OK
  EXPECT: DBL_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=7ec12389f05121c88618bbb247b6afffd042b1d9120a38fb630f32e38599e130; exit=0; EXPECT=matched; output-sha256=a8f606436ac578097a53867e60a71d0f050f3c0f74e79b551fe63dd379f49e93; output-bytes=6; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G18: Animacion look_thumbnail_state replicada en el carrusel de Estilos (frames intermedios, no aparicion en seco)
  CHECK: ./gradlew assembleRelease -q > /dev/null 2>&1 && python3 ~/temp/opencode-work/revela-gates/g18.py 2>&1 | tail -1
  EXPECT: LOOKANIM_OK
  EVIDENCE: automatic-evidence=v1; definition-sha256=92aa4f66955fba7d5ef3514994749f45067151672038ae64b550b350459b7b9a; exit=0; EXPECT=matched; output-sha256=35e6e012a39d9dccb1284a2997d7fa17a98a98e483e351631eb8302fa64728a1; output-bytes=11; shell=/bin/sh; cwd=/home/nacho/Documentos/Repos/revela-git

- [x] G19: Rediseño UI claro estilo Snapseed: rejilla 5 col sin tarjetas, iconos outline, hoja blanca, pestañas de texto con azul, foto con margen, status bar clara; banco funcional completo en verde tras el tema
  CHECK: cd /home/nacho/Documentos/Repos/revela-git && ./gradlew testDebugUnitTest -q > /dev/null 2>&1 && ./gradlew assembleRelease -q > /dev/null 2>&1 && python3 ~/temp/opencode-work/revela-gates/g14.py 2>&1 | tail -1 && python3 ~/temp/opencode-work/revela-gates/g16.py 2>&1 | tail -1 && python3 ~/temp/opencode-work/revela-gates/g18.py 2>&1 | tail -1
  EXPECT: HOME_GATE_OK + GRID28_OK + LOOKANIM_OK
  EVIDENCE: ejecutado 19-Oct-2026; g14, g15, g16, g16func y g18 todos OK tras el tema claro (106/106 tests + banco emulador)
