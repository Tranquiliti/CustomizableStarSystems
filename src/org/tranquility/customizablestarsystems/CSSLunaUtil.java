package org.tranquility.customizablestarsystems;

import lunalib.lunaDebug.LunaDebug;
import lunalib.lunaSettings.LunaSettings;

// Utility class to avoid a Java bug with importing LunaLib classes in an extended BaseModPlugin,
// causing the LunaLib soft dependency to become a hard dependency
public class CSSLunaUtil {
    public static void addSnippet() {
        LunaDebug.addSnippet(new CSSSpawnStarSystemsSnippet());
    }

    public static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }
}