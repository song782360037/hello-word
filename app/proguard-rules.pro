# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.aichat.data.model.** { *; }
