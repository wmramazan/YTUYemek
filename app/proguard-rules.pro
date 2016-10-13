-ignorewarnings

-keep class com.loopj.android.http.SerializableCookie {*;}
-assumenosideeffects class android.util.Log {
    public static *** d(...);
}