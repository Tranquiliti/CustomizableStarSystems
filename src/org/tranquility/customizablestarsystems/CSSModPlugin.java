package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({"unused", "unchecked"})
public class CSSModPlugin extends BaseModPlugin {
    private transient HashMap<MarketAPI, String> marketsToOverrideAdmin;
    private transient StarSystemAPI teleportSystem;

    @Override
    public void onApplicationLoad() {
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) CSSLunaUtil.addSnippet();
    }

    // If a teleport system exists, teleport the player there on new game (doing this before game load crashes the game)
    @Override
    public void onGameLoad(boolean newGame) {
        if (newGame && teleportSystem != null) {
            CSSUtil.teleportPlayerToSystem(teleportSystem);
            teleportSystem = null;
        }
    }

    // Generates mod systems after proc-gen so that planet markets can properly generate
    @Override
    public void onNewGameAfterProcGen() {
        SettingsAPI settings = Global.getSettings();

        boolean doCustomStarSystems;
        String enableSystemId = settings.getString("customizablestarsystems", "settings_enableCustomStarSystems");
        if (settings.getModManager().isModEnabled("lunalib"))
            doCustomStarSystems = Boolean.TRUE.equals(CSSLunaUtil.getBoolean(settings.getString("customizablestarsystems", "mod_id_customizablestarsystems"), enableSystemId));
        else doCustomStarSystems = settings.getBoolean(enableSystemId);

        if (doCustomStarSystems) try {
            generateSystems();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Gives selected markets their admins (doing this before economy load crashes the game)
    @Override
    public void onNewGameAfterEconomyLoad() {
        CSSUtil.generateAdminsOnMarkets(marketsToOverrideAdmin);
        marketsToOverrideAdmin = null;
    }

    private void generateSystems() throws JSONException, IOException {
        JSONObject systems = CSSUtil.getMergedSystemJSON();
        CSSUtil util = new CSSUtil();
        for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
            String systemId = it.next();
            JSONObject systemOptions = systems.getJSONObject(systemId);
            if (systemOptions.optBoolean(util.OPT_IS_ENABLED, true))
                for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                    StarSystemAPI newSystem = util.generateCustomStarSystem(systemOptions, systemId);
                    if (systemOptions.optBoolean(util.OPT_TELEPORT_UPON_GENERATION, false)) teleportSystem = newSystem;
                    Global.getLogger(CSSModPlugin.class).info(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generatedSystem"), systemId));
                }
            else
                Global.getLogger(CSSModPlugin.class).info(String.format(Global.getSettings().getString("customizablestarsystems", "commands_disabledSystem"), systemId));
        }
        marketsToOverrideAdmin = util.marketsToOverrideAdmin;
    }
}