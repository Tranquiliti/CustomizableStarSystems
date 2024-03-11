package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import lunalib.lunaDebug.LunaSnippet;
import lunalib.lunaDebug.SnippetBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CSSSpawnStarSystemsSnippet extends LunaSnippet {
    @Override
    public String getName() {
        return Global.getSettings().getString("customizablestarsystems", "snippets_spawnSystemName");
    }

    @Override
    public String getDescription() {
        return Global.getSettings().getString("customizablestarsystems", "snippets_spawnSystemDesc");
    }

    @Override
    public String getModId() {
        return "customizablestarsystems";
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
        } catch (Exception e) {
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
            output.addPara(Global.getSettings().getString("customizablestarsystems", "snippets_spawnSystemNoSelected"), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
            return;
        }

        JSONObject systems;
        try {
            systems = CSSUtil.getMergedSystemJSON();
        } catch (Exception e) { // Re-using Console Commands message here; it's economical!
            output.addPara(Global.getSettings().getString("customizablestarsystems", "commands_error_badJSON") + e, 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            return;
        }

        // Validate all enabled system IDs, as the file may have changed a system's ID
        for (String systemId : enabledParams)
            if (!systems.has(systemId)) {
                output.addPara(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_noSystemId"), systemId), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        StarSystemAPI teleportSystem = null;
        StringBuilder print = new StringBuilder();
        CSSUtil util = new CSSUtil();
        for (String systemId : enabledParams)
            try { // Generate all selected custom star systems
                JSONObject systemOptions = systems.getJSONObject(systemId);
                for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                    StarSystemAPI newSystem = util.generateCustomStarSystem(systemOptions, systemId);
                    if (systemOptions.optBoolean(util.OPT_TELEPORT_UPON_GENERATION, false)) teleportSystem = newSystem;
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generatedSystem"), systemId));
                }
            } catch (Exception e) {
                print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_badSystem"), systemId));
                output.addPara(print.append(e).toString(), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        if (teleportSystem != null) CSSUtil.teleportPlayerToSystem(teleportSystem);
        CSSUtil.generateAdminsOnMarkets(util.marketsToOverrideAdmin);

        output.addPara(print.toString(), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
    }
}