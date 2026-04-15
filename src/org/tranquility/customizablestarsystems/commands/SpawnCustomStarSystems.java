package org.tranquility.customizablestarsystems.commands;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommandWithSuggestion;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.tranquility.customizablestarsystems.CustomStarSystem;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;
import static org.tranquility.customizablestarsystems.CSSUtil.*;

public class SpawnCustomStarSystems implements BaseCommandWithSuggestion {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty()) return CommandResult.BAD_SYNTAX;
        String[] params = args.split(" ");
        if (params.length < 1) return CommandResult.BAD_SYNTAX;

        JSONObject systems;
        try {
            systems = getMergedSystemJSON();
        } catch (JSONException | IOException e) {
            Console.showException(COMMANDS_ERROR_BAD_JSON, e);
            return CommandResult.ERROR;
        }

        StringBuilder print = new StringBuilder();
        StarSystemAPI teleportSystem = null;
        List<Constellation> constellations = getProcgenConstellations();
        Map<MarketAPI, String> marketsToOverrideAdmin = new HashMap<>();
        if (params[0].equals("all")) {
            // Generate all enabled custom star systems
            for (String systemId : getCustomStarSystemIds()) {
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    if (systemOptions.optBoolean(OPT_IS_ENABLED, true))
                        teleportSystem = generateSystem(systemOptions, systemId, print, teleportSystem, constellations, marketsToOverrideAdmin);
                    else print.append(String.format(COMMANDS_DISABLED_SYSTEM, systemId));
                } catch (JSONException e) {
                    return showBadSystemException(systemId, print, e);
                }
            }
        } else {
            // Verify first that arguments only contain valid ids before creating any star systems
            for (String systemId : params)
                if (!systems.has(systemId)) {
                    Console.showMessage(String.format(COMMANDS_ERROR_NO_SYSTEM_ID, systemId));
                    return CommandResult.ERROR;
                }

            // Generate selected custom star systems
            for (String systemId : params)
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    teleportSystem = generateSystem(systemOptions, systemId, print, teleportSystem, constellations, marketsToOverrideAdmin);
                } catch (JSONException e) {
                    return showBadSystemException(systemId, print, e);
                }
        }

        if (teleportSystem != null) teleportPlayerToSystem(teleportSystem);
        generateAdminsOnMarkets(marketsToOverrideAdmin);

        Console.showMessage(print);
        return CommandResult.SUCCESS;
    }

    @Override
    public List<String> getSuggestions(int parameter, List<String> previous, CommandContext context) {
        return getCustomStarSystemIds();
    }

    private StarSystemAPI generateSystem(JSONObject systemOptions, String systemId, StringBuilder print, StarSystemAPI teleportSystem, List<Constellation> constellations, Map<MarketAPI, String> marketsToOverrideAdmin) throws JSONException {
        for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
            CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId, constellations, marketsToOverrideAdmin, true);
            if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false)) teleportSystem = newSystem.getSystem();

            print.append(String.format(COMMANDS_GENERATED_SYSTEM, systemId));
        }
        return teleportSystem;
    }

    private CommandResult showBadSystemException(String systemId, StringBuilder print, Exception e) {
        print.append(String.format(COMMANDS_ERROR_BAD_SYSTEM, systemId));
        Console.showException(print, e);
        return CommandResult.ERROR;
    }
}