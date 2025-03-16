package org.tranquility.customizablestarsystems.lunalib;

import lunalib.lunaDebug.LunaDebug;
import lunalib.lunaSettings.LunaSettings;

// Utility class to avoid a crash due to importing LunaLib classes in an extended BaseModPlugin,
// causing the LunaLib soft dependency to become a hard dependency
public final class CSSLunaUtil {
    // TODO: check if this class is necessary in 0.98a, with the Java 17 update
    public static void addSnippet() {
        LunaDebug.addSnippet(new SpawnStarSystemsSnippet());
    }

    public static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }
}