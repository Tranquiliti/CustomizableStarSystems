package org.tranquility.customizablestarsystems;

import com.fs.starfarer.api.Global;
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
    //The name displayed on top of the Snippet.
    @Override
    public String getName() {
        return Global.getSettings().getString("customizablestarsystems", "snippets_spawn_system_name");
    }

    //The Description of the Snippet. The length of the Description slightly increases the size of the Snippet, but not by a lot.
    //If your Snippet has a long description and not many parameters, use the builder.addSpace(); method in addParameters to increase the size of the Snippet.
    @Override
    public String getDescription() {
        return Global.getSettings().getString("customizablestarsystems", "snippets_spawn_system_desc");
    }

    //Required to display which mod the Snippet is from.
    @Override
    public String getModId() {
        return "customizablestarsystems";
    }

    //Represents the tags on the left side of the Snippet Menu. The SnippetTags Enum provides the main preset Tags available, but returning a Unique String will
    //create a new Tag and add it to the list.
    @Override
    public List<String> getTags() {
        List<String> tags = new ArrayList<>();
        tags.add(SnippetTags.Cheat.toString());
        tags.add(SnippetTags.Entity.toString());
        return tags;
    }

    //Called when the Snippet is being created. It allows you to add Parameters to the Snippet. The "Key" String can be received in the execute method.
    @Override
    public void addParameters(SnippetBuilder builder) {
        JSONObject systems;
        try {
            systems = Global.getSettings().getMergedJSONForMod(Global.getSettings().getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems");
        } catch (Exception e) {
            return;
        }

        for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
            String systemId = it.next();
            builder.addBooleanParameter(systemId, systemId, false);
        }
    }

    //Called when the "Execute" has been pressed.
    //The parameters variable contains the value of all parameters added from addParameters, getabble through their key String.
    //The output variable allows to add paragraphs, images, etc to the output panel.
    @Override
    public void execute(Map<String, Object> parameters, TooltipMakerAPI output) {
        List<String> enabled = new ArrayList<>();

        for (String param : parameters.keySet())
            if ((Boolean) parameters.get(param)) enabled.add(param);

        // Do nothing if nothing is selected
        if (enabled.isEmpty()) {
            output.addPara(Global.getSettings().getString("customizablestarsystems", "snippets_spawn_system_no_selected"), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());
            return;
        }

        JSONObject systems;
        try {
            systems = Global.getSettings().getMergedJSONForMod(Global.getSettings().getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems");
        } catch (Exception e) { // Re-using Console Commands message here; it's economical!
            output.addPara(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_json"), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
            return;
        }

        for (String systemId : enabled)
            if (!systems.has(systemId)) {
                output.addPara(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_no_system_id"), systemId), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        CSSUtil util = new CSSUtil();
        StringBuilder print = new StringBuilder();
        // Generate only custom star systems with specified ids
        for (String systemId : enabled)
            try {
                JSONObject systemOptions = systems.getJSONObject(systemId);
                for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                    util.generateCustomStarSystem(systemOptions);
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generated_system"), systemId)).append("\n");
                }
            } catch (Exception e) {
                print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_system"), systemId));
                output.addPara(print.toString(), 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor());
                return;
            }

        output.addPara(print.toString(), 0f, Misc.getPositiveHighlightColor(), Misc.getHighlightColor());

        CSSUtil.generateAdminsOnCustomStarSystems(util.marketsToOverrideAdmin);
    }
}