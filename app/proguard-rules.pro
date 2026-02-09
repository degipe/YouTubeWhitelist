# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes used for serialization
-keep,includedescriptorclasses class io.github.degipe.youtubewhitelist.**$$serializer { *; }
-keepclassmembers class io.github.degipe.youtubewhitelist.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.degipe.youtubewhitelist.** {
    kotlinx.serialization.KSerializer serializer(...);
}
