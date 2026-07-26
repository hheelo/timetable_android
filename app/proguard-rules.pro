# ============================================================
# ProGuard rules for timetable_android
# ============================================================

# --- Kotlin Serialization ---
# 只保留序列化真正需要的东西。早期版本这里有
# `-keep class kotlinx.serialization.** { *; }`，等于把整个序列化库排除在 R8 之外，
# 收益为零、体积代价很大，已移除。
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault, InnerClasses

# 本项目自己的 @Serializable 类：显式保留其生成的 serializer。
-keep,includedescriptorclasses class com.hheelo.countdown.CountdownEvent { *; }
-keepclassmembers class com.hheelo.countdown.CountdownEvent {
    *** Companion;
}
-keepclasseswithmembers class com.hheelo.countdown.CountdownEvent$$serializer {
    *;
}

# kotlinx.serialization 官方推荐的条件规则：只对带 @Serializable 的类生效，
# 而不是无差别保留整个库。
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
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
