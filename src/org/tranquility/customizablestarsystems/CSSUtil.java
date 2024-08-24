package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;

/**
 * A utility class for the Customizable Star Systems mod
 */
public final class CSSUtil {
    /**
     * Get the merged JSON for the custom star systems file
     *
     * @return A JSONObject representing the custom star systems file
     */
    public static JSONObject getMergedSystemJSON() throws JSONException, IOException {
        return Global.getSettings().getMergedJSONForMod(PATH_MERGED_JSON_CUSTOM_STAR_SYSTEMS, MOD_ID_CUSTOMIZABLE_STAR_SYSTEMS);
    }

    /**
     * Sets admins accordingly on given markets; the Map is cleared afterward
     *
     * @param marketMap A map of market IDs to the admins to place
     */
    public static void generateAdminsOnMarkets(Map<MarketAPI, String> marketMap) {
        if (marketMap != null && !marketMap.isEmpty()) {
            AICoreAdminPluginImpl aiPlugin = new AICoreAdminPluginImpl();
            for (MarketAPI market : marketMap.keySet())
                switch (marketMap.get(market)) {
                    case Factions.PLAYER:
                        market.setAdmin(null);
                        break;
                    case Commodities.ALPHA_CORE:
                        market.setAdmin(aiPlugin.createPerson(Commodities.ALPHA_CORE, market.getFaction().getId(), 0));
                        break;
                }
            marketMap.clear(); // No need for the HashMap afterward, so clear it just in case
        }
    }

    /**
     * Teleports the player to a given star system; should only be run if the player fleet is loaded in the game
     *
     * @param system The system to which the player gets teleported
     */
    public static void teleportPlayerToSystem(StarSystemAPI system) {
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        PlanetAPI star = system.getStar();
        player.getContainingLocation().removeEntity(player);
        star.getContainingLocation().addEntity(player);
        Global.getSector().setCurrentLocation(star.getContainingLocation());
        player.setNoEngaging(2f);
        player.clearAssignments();
    }
}