package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import lunalib.lunaDebug.LunaDebug;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({"unused", "unchecked"})
public class CSSModPlugin extends BaseModPlugin {
    private transient HashMap<MarketAPI, String> marketsToOverrideAdmin;

    // Adding LunaSnippet for spawning in custom star systems
    @Override
    public void onApplicationLoad() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib"))
            LunaDebug.addSnippet(new CSSSpawnStarSystemsSnippet());
    }

    // Generates mod systems after proc-gen so that planet markets can properly generate
    @Override
    public void onNewGameAfterProcGen() {
        SettingsAPI settings = Global.getSettings();

        boolean doCustomStarSystems;
        String enableSystemId = settings.getString("customizablestarsystems", "settings_enableCustomStarSystems");
        if (settings.getModManager().isModEnabled("lunalib"))
            doCustomStarSystems = Boolean.TRUE.equals(LunaSettings.getBoolean(settings.getString("customizablestarsystems", "mod_id_customizablestarsystems"), enableSystemId));
        else doCustomStarSystems = settings.getBoolean(enableSystemId);

        if (doCustomStarSystems) try {
            JSONObject systems = settings.getMergedJSONForMod(settings.getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems");
            CSSUtil util = new CSSUtil();

            // Generate each custom star system from merged customStarSystems.json
            for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
                String systemId = it.next();
                JSONObject systemOptions = systems.getJSONObject(systemId);
                if (systemOptions.optBoolean(util.OPT_IS_ENABLED, true))
                    for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                        util.generateCustomStarSystem(systemOptions);
                        Global.getLogger(CSSModPlugin.class).info(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generated_system"), systemId));
                    }
                else
                    Global.getLogger(CSSModPlugin.class).info(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_system"), systemId));
            }
            marketsToOverrideAdmin = util.marketsToOverrideAdmin;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        // Gives selected markets the admins they're supposed to have (can't do it before economy load)
        CSSUtil.generateAdminsOnCustomStarSystems(marketsToOverrideAdmin);
        marketsToOverrideAdmin = null;
    }
}