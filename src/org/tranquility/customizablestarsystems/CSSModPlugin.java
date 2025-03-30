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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;
import static org.tranquility.customizablestarsystems.CSSUtil.LUNALIB_ENABLED;

@SuppressWarnings({"unused", "unchecked"})
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
            CSSUtil.teleportPlayerToSystem(teleportSystem);
            teleportSystem = null;
        }
    }

    // Generates mod systems after proc-gen so that planet markets can properly generate
    @Override
    public void onNewGameAfterProcGen() {
        boolean doCustomStarSystems = LUNALIB_ENABLED ? Boolean.TRUE.equals(LunaSettings.getBoolean(MOD_ID_CUSTOMIZABLE_STAR_SYSTEMS, SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS.replace(MOD_ID_CUSTOMIZABLE_STAR_SYSTEMS + '_', ""))) : Global.getSettings().getBoolean(SETTINGS_ENABLE_CUSTOM_STAR_SYSTEMS);
        if (doCustomStarSystems) try {
            generateCustomStarSystems();
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

    private void generateCustomStarSystems() throws JSONException, IOException {
        JSONObject systems = CSSUtil.getMergedSystemJSON();
        List<Constellation> constellations = CSSUtil.getProcgenConstellations();
        marketsToOverrideAdmin = new HashMap<>();
        for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
            String systemId = it.next();
            JSONObject systemOptions = systems.getJSONObject(systemId);
            if (systemOptions.optBoolean(OPT_IS_ENABLED, true))
                for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
                    CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId, constellations, marketsToOverrideAdmin, false);
                    if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false))
                        teleportSystem = newSystem.getSystem();

                    Global.getLogger(CSSModPlugin.class).info(String.format(COMMANDS_GENERATED_SYSTEM, systemId));
                }
            else Global.getLogger(CSSModPlugin.class).info(String.format(COMMANDS_DISABLED_SYSTEM, systemId));
        }
    }
}