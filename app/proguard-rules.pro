# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html

-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-adaptresourcefilenames
-adaptresourcefilecontents
# Optimization is turned off by default. Dex does not like code run
# through the ProGuard optimize and preverify steps (and performs some
# of these optimizations on its own).
-dontoptimize
-dontpreverify
# Note that if you want to enable optimization, you cannot just
# include optimization flags in your own project configuration file;
# instead you will need to point to the
# "proguard-android-optimize.txt" file instead of this one from your
# project.properties file.
-keep class com.google.zxing.** { *; }
-keep class android.widget.** { *; }
-keep class android.support.v7.appcompat.** { *; }
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }
-keep public class com.activeandroid.** { *; }
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class org.ocpsoft.prettytime.** { *; }
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keepattributes Exception
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes *Annotation*

-keep public class com.sgo.orimakardaya.Beans.** extends com.activeandroid.Model { *; }
-keep public class com.sgo.orimakardaya.Beans.** { *; }
#-keep public class com.sgo.orimakardaya.Beans.** extends com.activeandroid.Model { *; }
#-keep public class com.sgo.orimakardaya.Beans.** { *; }
-keep class com.sgo.orimakardaya.activities.CreateGroupActivity$TempObjectData { *; }

-keep class com.sgo.orimakardaya.coreclass.BalanceHandler { *; }
-keep class com.sgo.orimakardaya.coreclass.CollapseExpandAnimation { *; }
-keep class com.sgo.orimakardaya.coreclass.Contents { *; }
-keep class com.sgo.orimakardaya.coreclass.CoreApp { *; }
-keep class com.sgo.orimakardaya.coreclass.CurrencyFormat { *; }
-keep class com.sgo.orimakardaya.coreclass.HideKeyboard { *; }
-keep class com.sgo.orimakardaya.coreclass.InetHandler { *; }
-keep class com.sgo.orimakardaya.coreclass.LifeCycleHandler { *; }
-keep class com.sgo.orimakardaya.securities.Md5 { *; }
-keep class com.sgo.orimakardaya.coreclass.MyApiClient { *; }
-keep class com.sgo.orimakardaya.coreclass.MyPicasso { *; }
-keep class com.sgo.orimakardaya.coreclass.NotificationActionView { *; }
-keep class com.sgo.orimakardaya.coreclass.PeriodTime { *; }
-keep class com.sgo.orimakardaya.coreclass.RoundedImageView { *; }
-keep class com.sgo.orimakardaya.coreclass.QRCodeEncoder { *; }
-keep class com.sgo.orimakardaya.coreclass.WebParams { *; }

-keep class com.sgo.orimakardaya.dialogs.DefinedDialog { *; }
-keep class com.sgo.orimakardaya.dialogs.ReportBillerDialog { *; }
-keep class com.sgo.orimakardaya.dialogs.AlertDialogFrag { *; }

-keep class com.sgo.orimakardaya.fragments.ListCollectionPayment$TempObjectData { *; }

-keep class com.sgo.orimakardaya.fragments.FragAskForMoney$TempObjectData { *; }
-keep class com.sgo.orimakardaya.fragments.FragPayFriends$TempObjectData { *; }
-keep class com.sgo.orimakardaya.fragments.FragPayFriendsConfirm$TempTxID { *; }
-keep class com.sgo.orimakardaya.fragments.ListBillerMerchant$ListObject{ *; }

-keep class com.sgo.orimakardaya.services.BalanceService { *; }
-keep class io.codetail.animation.arcanimator.** { *; }
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


# The support library contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version.  We know about them, and they are safe.
-dontwarn android.support.**
-dontwarn okio.**
-dontwarn org.joda.time.**

# RxAndroid
-dontwarn rx.internal.util.unsafe.**
