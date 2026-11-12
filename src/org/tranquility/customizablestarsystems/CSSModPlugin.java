package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import lunalib.lunaDebug.LunaDebug;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONException;
import org.json.JSONObject;
import org.tranquility.customizablestarsystems.lunalib.SpawnStarSystemsSnippet;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;
import static org.tranquility.customizablestarsystems.CSSUtil.*;

@SuppressWarnings("unused")
public class CSSModPlugin extends BaseModPlugin {
    private transient StarSystemAPI teleportSystem;
    private transient Map<MarketAPI, String> marketsToOverrideAdmin;

    @Override
    public void onApplicationLoad() {
        if (LUNALIB_ENABLED) LunaDebug.addSnippet(new SpawnStarSystemsSnippet());
    }

    // If a teleport system exists, teleport the player there on new game (doing this before game load crashes the game)
    @Override
    public void onGameLoad(boolean newGame) {
        if (newGame && teleportSystem != null) {
            teleportPlayerToSystem(teleportSystem);
            teleportSystem = null;
        }
    }

    // Generates mod systems after proc-gen so that planet markets can properly generate
    @Override
    public void onNewGameAfterProcGen() {
        Boolean enableCSS;
        if (LUNALIB_ENABLED) {
            enableCSS = LunaSettings.getBoolean(MOD_ID, SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS);
            if (enableCSS == null) enableCSS = getSettingBoolean(SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS);
        } else enableCSS = getSettingBoolean(SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS);

        if (enableCSS) generateCustomStarSystems();
    }

    // Gives selected markets their admins (doing this before economy load crashes the game)
    @Override
    public void onNewGameAfterEconomyLoad() {
        generateAdminsOnMarkets(marketsToOverrideAdmin);
        marketsToOverrideAdmin = null;
    }

    private void generateCustomStarSystems() {
        JSONObject systems;
        try {
            systems = getMergedSystemJSON();
        } catch (JSONException | IOException e) {
            Global.getLogger(CSSModPlugin.class).error(COMMANDS_ERROR_BAD_JSON, e);
            return;
        }

        List<Constellation> constellations = getProcgenConstellations();
        marketsToOverrideAdmin = new HashMap<>();
        for (String systemId : getCustomStarSystemIds())
            try {
                JSONObject systemOptions = systems.getJSONObject(systemId);
                if (systemOptions.optBoolean(OPT_IS_ENABLED, true))
                    for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
                        CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId, constellations, marketsToOverrideAdmin, false);
                        if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false))
                            teleportSystem = newSystem.getSystem();

                        Global.getLogger(CSSModPlugin.class).info(COMMANDS_GENERATED_SYSTEM.formatted(systemId));
                    }
                else Global.getLogger(CSSModPlugin.class).info(COMMANDS_DISABLED_SYSTEM.formatted(systemId));
            } catch (JSONException e) {
                Global.getLogger(CSSModPlugin.class).error(COMMANDS_ERROR_BAD_SYSTEM.formatted(systemId), e);
            }
    }
}