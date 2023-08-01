package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lunalib.lunaSettings.LunaSettings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

@SuppressWarnings("unused")
public class CSSModPlugin extends BaseModPlugin {
    private transient HashMap<MarketAPI, String> marketsToOverrideAdmin;

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
            JSONArray systemList = settings.getMergedJSONForMod(settings.getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems").getJSONArray(settings.getString("customizablestarsystems", "settings_customStarSystems"));
            CSSUtil util = new CSSUtil();
            for (int i = 0; i < systemList.length(); i++) {
                JSONObject systemOptions = systemList.getJSONObject(i);
                if (systemOptions.optBoolean(util.OPT_IS_ENABLED, true))
                    for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--)
                        util.generateCustomStarSystem(systemOptions);
            }
            marketsToOverrideAdmin = util.marketsToOverrideAdmin;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNewGameAfterEconomyLoad() {
        // Gives selected markets the admins they're supposed to have (can't do it before economy load)
        if (marketsToOverrideAdmin != null) {
            AICoreAdminPluginImpl aiPlugin = new AICoreAdminPluginImpl();
            for (MarketAPI market : marketsToOverrideAdmin.keySet())
                switch (marketsToOverrideAdmin.get(market)) {
                    case Factions.PLAYER:
                        market.setAdmin(null);
                        break;
                    case Commodities.ALPHA_CORE:
                        market.setAdmin(aiPlugin.createPerson(Commodities.ALPHA_CORE, market.getFaction().getId(), 0));
                }
            // No need for the HashMap afterwards, so clear it and set it to null to minimize memory use, just in case
            marketsToOverrideAdmin.clear();
            marketsToOverrideAdmin = null;
        }
    }
}