package data.console.commands;

import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.tranquility.customizablestarsystems.CSSUtil;
import org.tranquility.customizablestarsystems.CustomStarSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.tranquility.customizablestarsystems.CSSStrings.*;

public class SpawnCustomStarSystems implements BaseCommand {
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
            systems = CSSUtil.getMergedSystemJSON();
        } catch (Exception e) {
            Console.showMessage(COMMANDS_ERROR_BAD_JSON + e);
            return CommandResult.ERROR;
        }

        StringBuilder print = new StringBuilder();
        StarSystemAPI teleportSystem = null;
        Map<MarketAPI, String> marketsToOverrideAdmin = new HashMap<MarketAPI, String>(0);
        if (params[0].equals("all")) {
            // Generate all enabled custom star systems
            for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
                String systemId = (String) it.next();
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    if (systemOptions.optBoolean(OPT_IS_ENABLED, true))
                        teleportSystem = generateSystem(systemOptions, systemId, print, teleportSystem, marketsToOverrideAdmin);
                    else print.append(String.format(COMMANDS_DISABLED_SYSTEM, systemId));
                } catch (Exception e) {
                    return showBadSystemError(systemId, print, e);
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
                    teleportSystem = generateSystem(systemOptions, systemId, print, teleportSystem, marketsToOverrideAdmin);
                } catch (Exception e) {
                    return showBadSystemError(systemId, print, e);
                }
        }

        if (teleportSystem != null) CSSUtil.teleportPlayerToSystem(teleportSystem);
        CSSUtil.generateAdminsOnMarkets(marketsToOverrideAdmin);

        Console.showMessage(print);
        return CommandResult.SUCCESS;
    }

    private StarSystemAPI generateSystem(JSONObject systemOptions, String systemId, StringBuilder print, StarSystemAPI teleportSystem, Map<MarketAPI, String> marketsToOverrideAdmin) throws JSONException {
        for (int numOfSystems = systemOptions.optInt(OPT_NUMBER_OF_SYSTEMS, CustomStarSystem.DEFAULT_NUMBER_OF_SYSTEMS); numOfSystems > 0; numOfSystems--) {
            CustomStarSystem newSystem = new CustomStarSystem(systemOptions, systemId);
            if (systemOptions.optBoolean(OPT_TELEPORT_UPON_GENERATION, false)) teleportSystem = newSystem.getSystem();

            marketsToOverrideAdmin.putAll(newSystem.getMarketsToOverrideAdmin());

            print.append(String.format(COMMANDS_GENERATED_SYSTEM, systemId));
        }
        return teleportSystem;
    }

    private CommandResult showBadSystemError(String systemId, StringBuilder print, Exception e) {
        print.append(String.format(COMMANDS_ERROR_BAD_SYSTEM, systemId));
        Console.showMessage(print.append(e));
        return CommandResult.ERROR;
    }
}