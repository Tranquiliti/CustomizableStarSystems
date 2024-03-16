package org.tranquility.customizablestarsystems.lunalib;

import com.fs.starfarer.api.Global;
import lunalib.lunaDebug.LunaDebug;
import lunalib.lunaSettings.LunaSettings;

// Utility class to avoid a Java bug with importing LunaLib classes in an extended BaseModPlugin,
// causing the LunaLib soft dependency to become a hard dependency
public final class CSSLunaUtil {
    public static final boolean LUNALIB_ENABLED = Global.getSettings().getModManager().isModEnabled("lunalib");

    public static void addSnippet() {
        LunaDebug.addSnippet(new SpawnStarSystemsSnippet());
    }

    public static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }
}