package data.console.commands;

import com.fs.starfarer.api.Global;
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
        String[] tmp = args.split(" ");
        if (tmp.length < 1) return CommandResult.BAD_SYNTAX;

        JSONObject systems;
        try {
            systems = Global.getSettings().getMergedJSONForMod(Global.getSettings().getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems");
        } catch (Exception e) {
            Console.showMessage(Global.getSettings().getString("customizablestarsystems", "commands_error_badJSON") + e);
            return CommandResult.ERROR;
        }

        CSSUtil util = new CSSUtil();
        StringBuilder print = new StringBuilder();
        if (tmp[0].equals("all")) {
            // Generate each custom star system from merged customStarSystems.json
            for (Iterator<String> it = systems.keys(); it.hasNext(); ) {
                String systemId = (String) it.next();
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    if (systemOptions.optBoolean(util.OPT_IS_ENABLED, true))
                        for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                            util.generateCustomStarSystem(systemOptions);
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
            for (String systemId : tmp)
                if (!systems.has(systemId)) {
                    Console.showMessage(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_noSystemId"), systemId));
                    return CommandResult.ERROR;
                }

            for (String systemId : tmp)
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                        util.generateCustomStarSystem(systemOptions);
                        print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generatedSystem"), systemId));
                    }
                } catch (Exception e) {
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_badSystem"), systemId));
                    Console.showMessage(print);
                    return CommandResult.ERROR;
                }
        }

        Console.showMessage(print);
        CSSUtil.generateAdminsOnMarkets(util.marketsToOverrideAdmin);

        return CommandResult.SUCCESS;
    }
}