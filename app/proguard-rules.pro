# revela: reglas R8
# kotlinx-serialization: mantiene serializers generados
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class app.revela.core.** { kotlinx.serialization.KSerializer serializer(...); }
