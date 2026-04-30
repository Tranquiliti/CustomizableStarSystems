package org.tranquility.customizablestarsystems.lunalib;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaDebug.LunaSnippet;
import lunalib.lunaDebug.SnippetBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.tranquility.customizablestarsystems.CustomStarSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;
import static org.tranquility.customizablestarsystems.CSSUtil.*;

public class SpawnStarSystemsSnippet extends LunaSnippet {
    @Override
    public String getName() {
        return SNIPPETS_SPAWN_SYSTEM_NAME;
    }

    @Override
    public String getDescription() {
        return SNIPPETS_SPAWN_SYSTEM_DESC;
    }

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        tags.add(SnippetTags.Cheat.toString());
        tags.add(SnippetTags.Entity.toString());
        return tags;
    }

    @Override
    public void addParameters(SnippetBuilder builder) {
        try {
            getMergedSystemJSON();
        } catch (JSONException | IOException e) {
            // Signifies a JSON error, so it does not matter if the "button" looks ugly
            builder.addBooleanParameter(COMMANDS_ERROR_BAD_JSON, COMMANDS_ERROR_BAD_JSON, true);
            return;
        }

        for (String systemId : getCustomStarSystemIds())
            builder.addBooleanParameter(systemId, systemId, false);
    }

    @Override
    public void execute(Map<String, Object> parameters, TooltipMakerAPI output) {
        List<String> enabledParams = new ArrayList<>();

        for (String param : parameters.keySet())
            if ((Boolean) parameters.get(param)) enabledParams.add(param);

        if (enabledParams.isEmpty()) {
            output.addPara(SNIPPETS_SPAWN_SYSTEM_NO_SELECTED, 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
            return;
        }

        generateSystems(enabledParams, output);
    }

    private void generateSystems(List<String> enabledParams, TooltipMakerAPI output) {
        JSONObject systems;
        try {
            systems = getMergedSystemJSON();
        } catch (JSONException | IOException e) { // Re-using Console Commands message here; it's economical!
            output.addPara(COMMANDS_ERROR_BAD_JSON + e, 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            return;
        }

        // Validate all enabled system IDs, as the file may have changed a system's ID
        for (String systemId : enabledParams)
            if (!systems.has(systemId)) {
                output.addPara(COMMANDS_ERROR_NO_SYSTEM_ID.formatted(systemId), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        StringBuilder print = new StringBuilder();
        StarSystemAPI teleportSystem = null;
        List<Constellation> constellations = getProcgenConstellations();
        Map<MarketAPI, String> marketsToOverrideAdmin = new HashMap<>();
        for (String systemId : enabledParams)
            try { // Generate all selected custom star systems
                JSONObject systemOptions = systems.getJSONObject(systemId);
                for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
                    CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId, constellations, marketsToOverrideAdmin, true);
                    if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false))
                        teleportSystem = newSystem.getSystem();

                    print.append(COMMANDS_GENERATED_SYSTEM.formatted(systemId));
                }
            } catch (JSONException e) {
                print.append(COMMANDS_ERROR_BAD_SYSTEM.formatted(systemId));
                output.addPara(print.toString() + e, 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                Global.getLogger(SpawnStarSystemsSnippet.class).error(print, e);
                return;
            }

        if (teleportSystem != null) teleportPlayerToSystem(teleportSystem);
        generateAdminsOnMarkets(marketsToOverrideAdmin);

        output.addPara(print.toString(), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
    }
}