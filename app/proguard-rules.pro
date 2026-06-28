# ============================================================
# ProGuard rules for timetable_android
# ============================================================

# --- Kotlin Serialization ---
# Keep @Serializable classes and their generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationCollector

-keep,includedescriptorclasses class com.hheelo.countdown.CountdownEvent { *; }
-keepclassmembers class com.hheelo.countdown.CountdownEvent {
    *** Companion;
}
-keepclasseswithmembers class com.hheelo.countdown.CountdownEvent$$serializer {
    *;
}

# Keep serializer infrastructure
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# --- Android Parcelable ---
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# --- Android Serializable ---
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# --- Glance AppWidget ---
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
