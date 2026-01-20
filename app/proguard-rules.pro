# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep debugging attributes for better crash reports
-keepattributes *Annotation*, Signature, Exception

# ========================================
# WanderState App Classes
# ========================================

# Keep MainActivity and all Activities
-keep public class com.example.wanderstate.MainActivity {
    public <init>(...);
}
-keep public class * extends android.app.Activity
-keep public class * extends androidx.activity.ComponentActivity

# Keep all app classes from being stripped
-keep class com.example.wanderstate.** { *; }
-keepclassmembers class com.example.wanderstate.** { *; }

# ========================================
# Jetpack Compose
# ========================================

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep classes that use @Composable
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ========================================
# Room Database
# ========================================

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }

# Keep Room generated classes
-keep class com.example.wanderstate.data.database.** { *; }
-keep class com.example.wanderstate.data.dao.** { *; }

# Keep data models and entities
-keep class com.example.wanderstate.data.models.** { *; }
-keepclassmembers class com.example.wanderstate.data.models.** { *; }

# ========================================
# Hilt Dependency Injection
# ========================================

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep classes annotated with Hilt annotations
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# ========================================
# Kotlin Serialization
# ========================================

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes
-keep,includedescriptorclasses class com.example.wanderstate.**$$serializer { *; }
-keepclassmembers class com.example.wanderstate.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.wanderstate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========================================
# Kotlin Coroutines
# ========================================

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========================================
# Coil Image Loading
# ========================================

# Keep Coil classes
-keep class coil.** { *; }
-keep interface coil.** { *; }

# ========================================
# AndroidSVG
# ========================================

# Keep AndroidSVG classes for map rendering
-keep class com.caverock.androidsvg.** { *; }

# ========================================
# Kotlin Reflect (if used)
# ========================================

-keep class kotlin.reflect.** { *; }
-keep class kotlin.Metadata { *; }

# ========================================
# General Android
# ========================================

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(***);
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}