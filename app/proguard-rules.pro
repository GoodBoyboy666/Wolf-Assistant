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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontobfuscate
-keep class top.goodboyboy.wolfassistant.ui.home.portal.model.** {*;}
-keep class top.goodboyboy.wolfassistant.ui.messagecenter.model.** {*;}
-keep class top.goodboyboy.wolfassistant.ui.personalcenter.personal.model.** {*;}
-keep class top.goodboyboy.wolfassistant.ui.schedulecenter.model.** {*;}
-keep class top.goodboyboy.wolfassistant.ui.servicecenter.model.** {*;}
-keep class top.goodboyboy.wolfassistant.common.** {*;}
-keep class top.goodboyboy.wolfassistant.util.GsonUtil {*;}
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
