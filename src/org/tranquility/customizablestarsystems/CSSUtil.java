package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.econ.reach.CommodityMarketData;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.*;

import static org.tranquility.customizablestarsystems.CSSStrings.MOD_ID;
import static org.tranquility.customizablestarsystems.CSSStrings.PATH_MERGED_JSON_CUSTOM_STAR_SYSTEMS;

/**
 * A utility class for the Customizable Star Systems mod
 */
public final class CSSUtil {
    public static final boolean LUNALIB_ENABLED = Global.getSettings().getModManager().isModEnabled("lunalib");
    private static List<String> customStarSystemIds;

    /**
     * Get the merged JSON for the custom star systems file. Also updates the list of custom star system IDs.
     *
     * @return A JSONObject representing the custom star systems file
     */
    @SuppressWarnings("unchecked")
    public static JSONObject getMergedSystemJSON() throws JSONException, IOException {
        JSONObject mergedJSON = Global.getSettings().getMergedJSONForMod(PATH_MERGED_JSON_CUSTOM_STAR_SYSTEMS, MOD_ID);
        if (customStarSystemIds == null) customStarSystemIds = new ArrayList<>(mergedJSON.length());
        else customStarSystemIds.clear();
        for (Iterator<String> iter = mergedJSON.keys(); iter.hasNext(); )
            customStarSystemIds.add(iter.next());

        return mergedJSON;
    }

    /**
     * Gets the currently-loaded list of custom star system IDs
     *
     * @return A List of custom star system IDs, or an unmodifiable empty List if the custom star systems file has not been loaded
     */
    public static List<String> getCustomStarSystemIds() {
        return customStarSystemIds != null ? customStarSystemIds : List.of();
    }

    public static boolean getSettingBoolean(String settingId) {
        try {
            return Global.getSettings().getJSONObject(MOD_ID).getBoolean(settingId);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
    }

    public static float getSettingFloat(String settingId) {
        try {
            return (float) Global.getSettings().getJSONObject(MOD_ID).getDouble(settingId);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
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
            marketMap.clear(); // No need for the Map afterward, so clear it just in case
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

    /**
     * Generates a list of proc-gen constellations, sorted by proximity from the current center of mass
     *
     * @return A list of proc-gen constellations
     */
    public static List<Constellation> getProcgenConstellations() {
        // Multiple star systems can have same constellation, which is why a Set is used here
        Set<Constellation> constellations = new HashSet<>();
        for (StarSystemAPI sys : Global.getSector().getStarSystems())
            if (sys.isProcgen() && sys.isInConstellation()) constellations.add(sys.getConstellation());

        List<Constellation> sortedConstellations = new ArrayList<>(constellations);
        final Vector2f centroidPoint = CommodityMarketData.computeCenterOfMass(null, null);
        sortedConstellations.sort((c1, c2) -> {
            if (c1 == c2) return 0;
            return Float.compare(Misc.getDistance(centroidPoint, c1.getLocation()), Misc.getDistance(centroidPoint, c2.getLocation()));
        });

        return sortedConstellations;
    }
}