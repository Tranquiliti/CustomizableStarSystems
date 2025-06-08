package org.tranquility.customizablestarsystems.lunalib;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaDebug.LunaSnippet;
import lunalib.lunaDebug.SnippetBuilder;
import org.json.JSONObject;
import org.tranquility.customizablestarsystems.CSSUtil;
import org.tranquility.customizablestarsystems.CustomStarSystem;

import java.util.*;

import static org.tranquility.customizablestarsystems.CSSStrings.*;

@SuppressWarnings("unchecked")
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
        return MOD_ID_CUSTOMIZABLE_STAR_SYSTEMS;
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
        JSONObject systems;
        try {
            systems = CSSUtil.getMergedSystemJSON();
        } catch (Exception e) { // This signifies a JSON error, so it does not matter if the "button" looks ugly
            builder.addBooleanParameter(COMMANDS_ERROR_BAD_JSON, COMMANDS_ERROR_BAD_JSON, true);
            return;
        }

        for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
            String systemId = it.next();
            builder.addBooleanParameter(systemId, systemId, false);
        }
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
            systems = CSSUtil.getMergedSystemJSON();
        } catch (Exception e) { // Re-using Console Commands message here; it's economical!
            output.addPara(COMMANDS_ERROR_BAD_JSON + e, 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            return;
        }

        // Validate all enabled system IDs, as the file may have changed a system's ID
        for (String systemId : enabledParams)
            if (!systems.has(systemId)) {
                output.addPara(String.format(COMMANDS_ERROR_NO_SYSTEM_ID, systemId), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        StringBuilder print = new StringBuilder();
        StarSystemAPI teleportSystem = null;
        List<Constellation> constellations = CSSUtil.getProcgenConstellations();
        Map<MarketAPI, String> marketsToOverrideAdmin = new HashMap<>();
        for (String systemId : enabledParams)
            try { // Generate all selected custom star systems
                JSONObject systemOptions = systems.getJSONObject(systemId);
                for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
                    CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId, constellations, marketsToOverrideAdmin, true);
                    if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false))
                        teleportSystem = newSystem.getSystem();

                    print.append(String.format(COMMANDS_GENERATED_SYSTEM, systemId));
                }
            } catch (Exception e) {
                print.append(String.format(COMMANDS_ERROR_BAD_SYSTEM, systemId)).append(e);
                output.addPara(print.toString(), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                Global.getLogger(SpawnStarSystemsSnippet.class).error(print, e);
                return;
            }

        if (teleportSystem != null) CSSUtil.teleportPlayerToSystem(teleportSystem);
        CSSUtil.generateAdminsOnMarkets(marketsToOverrideAdmin);

        output.addPara(print.toString(), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
    }
}