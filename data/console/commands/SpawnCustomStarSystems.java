package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.tranquility.customizablestarsystems.CSSUtil;

import java.util.Iterator;

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
            Console.showMessage(Global.getSettings().getString("customizablestarsystems", "commands_error_badJSON") + e);
            return CommandResult.ERROR;
        }

        StarSystemAPI teleportSystem = null;
        StringBuilder print = new StringBuilder();
        CSSUtil util = new CSSUtil();
        if (params[0].equals("all")) {
            for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
                String systemId = (String) it.next();
                try { // Generate all enabled custom star systems
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    if (systemOptions.optBoolean(util.OPT_IS_ENABLED, true))
                        for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                            StarSystemAPI newSystem = util.generateCustomStarSystem(systemOptions, systemId);
                            if (systemOptions.optBoolean(util.OPT_TELEPORT_UPON_GENERATION, false))
                                teleportSystem = newSystem;
                            print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generatedSystem"), systemId));
                        }
                    else
                        print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_disabledSystem"), systemId));
                } catch (Exception e) {
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_badSystem"), systemId));
                    Console.showMessage(print.append(e));
                    return CommandResult.ERROR;
                }
            }
        } else {
            // Verify first that arguments only contain valid ids before creating any star systems
            for (String systemId : params)
                if (!systems.has(systemId)) {
                    Console.showMessage(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_noSystemID"), systemId));
                    return CommandResult.ERROR;
                }

            for (String systemId : params)
                try { // Generate selected custom star systems
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                        StarSystemAPI newSystem = util.generateCustomStarSystem(systemOptions, systemId);
                        if (systemOptions.optBoolean(util.OPT_TELEPORT_UPON_GENERATION, false))
                            teleportSystem = newSystem;
                        print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generatedSystem"), systemId));
                    }
                } catch (Exception e) {
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_badSystem"), systemId));
                    Console.showMessage(print.append(e));
                    return CommandResult.ERROR;
                }
        }

        if (teleportSystem != null) CSSUtil.teleportPlayerToSystem(teleportSystem);
        CSSUtil.generateAdminsOnMarkets(util.marketsToOverrideAdmin);

        Console.showMessage(print);
        return CommandResult.SUCCESS;
    }
}