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

#keep eventbus
-keep @org.greenrobot.eventbus.Subscribe class * {*;}
-keep,allowobfuscation @interface org.greenrobot.eventbus.Subscribe
-keepclassmembernames class * {
    @org.greenrobot.eventbus.Subscribe *;
}
-keepattributes *Annotation*
-keepclassmembers class **{
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode {*;}
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent{
   <init>(java.lang.Throwable);
}

#keep litepal
-keep class org.litepal.**{*;}
-keepclassmembers class org.litepal.**{*;}
#keep DCS model
-keep class com.dynamsoft.camerasdk.model.internal.**{*;}
-keepclassmembers class com.dynamsoft.camerasdk.model.internal.**{*;}
