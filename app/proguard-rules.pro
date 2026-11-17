# Project specific R8/ProGuard rules.
# Release builds are minified and resource-shrunk (see app/build.gradle.kts).

# kotlinx.serialization: keep generated serializers for our @Serializable models
# (see https://github.com/Kotlin/kotlinx.serialization#android).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.katalon.katalianbanking.**$$serializer { *; }
-keepclassmembers class com.katalon.katalianbanking.** {
    *** Companion;
}
-keepclasseswithmembers class com.katalon.katalianbanking.** {
    kotlinx.serialization.KSerializer serializer(...);
}
