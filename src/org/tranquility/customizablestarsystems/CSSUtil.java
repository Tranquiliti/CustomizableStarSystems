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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    /**
     * Generates a list of proc-gen constellations, sorted by proximity from hyperspace center
     *
     * @return A list of proc-gen constellations
     */
    public static ArrayList<Constellation> getProcgenConstellations() {
        ArrayList<Constellation> constellations = new ArrayList<>();

        for (StarSystemAPI sys : Global.getSector().getStarSystems())
            if (sys.isProcgen() && sys.isInConstellation()) constellations.add(sys.getConstellation());

        Collections.sort(constellations, new Comparator<Constellation>() {
            final Vector2f centroidPoint = getHyperspaceCenter();

            public int compare(Constellation c1, Constellation c2) {
                if (c1 == c2) return 0;
                return Float.compare(Misc.getDistance(centroidPoint, c1.getLocation()), Misc.getDistance(centroidPoint, c2.getLocation()));
            }
        });

        return constellations;
    }

    /**
     * Gets the hyperspace center
     *
     * @return A 2D vector representing the hyperspace center
     */
    public static Vector2f getHyperspaceCenter() {
        try {
            JSONArray coordinates = Global.getSettings().getJSONArray(SETTINGS_HYPERSPACE_CENTER);
            return new Vector2f(coordinates.getInt(0), coordinates.getInt(1));
        } catch (JSONException e) {
            return new Vector2f(-4531, -5865); // Default values
        }
    }
}