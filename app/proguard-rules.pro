# YouTubeWhitelist ProGuard/R8 Rules

# ===== Kotlin Serialization =====
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their generated serializers
-keep,includedescriptorclasses class io.github.degipe.youtubewhitelist.**$$serializer { *; }
-keepclassmembers class io.github.degipe.youtubewhitelist.** {
    *** Companion;
}
-keepclasseswithmembers class io.github.degipe.youtubewhitelist.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Navigation Compose Route classes (type-safe navigation needs serialization at runtime)
-keep class io.github.degipe.youtubewhitelist.navigation.Route { *; }
-keep class io.github.degipe.youtubewhitelist.navigation.Route$* { *; }

# Export/Import DTOs (JSON serialization)
-keep class io.github.degipe.youtubewhitelist.core.export.model.** { *; }

# YouTube API DTOs (Retrofit + kotlinx-serialization converter)
-keep class io.github.degipe.youtubewhitelist.core.network.dto.** { *; }

# ===== WebView JavaScript Bridges =====
# @Keep + @JavascriptInterface — explicit rules as belt-and-suspenders
# R8 may strip private inner classes even with @Keep in some configurations
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface

# ===== Retrofit =====
# Keep Retrofit service interface methods (annotations are read via reflection)
-keep,allowobfuscation interface io.github.degipe.youtubewhitelist.core.network.api.YouTubeApiService {
    <methods>;
}
# Keep Retrofit annotation parameters
-keepattributes Signature
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# ===== OkHttp =====
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ===== Room =====
# Room entities and DAOs are processed by the annotation processor,
# but keep entities to be safe with reflection-based tools
-keep class io.github.degipe.youtubewhitelist.core.database.entity.** { *; }
-keep class io.github.degipe.youtubewhitelist.core.database.dao.** { *; }

# ===== Tink / Security Crypto =====
# ErrorProne annotations used by Google Tink (via security-crypto) are compile-time only
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# ===== Kotlin =====
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
