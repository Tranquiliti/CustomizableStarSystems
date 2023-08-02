package data.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.tranquility.customizablestarsystems.CSSUtil;

import java.util.HashMap;
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
            Console.showMessage(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_json"));
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
                            print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generated_system"), systemId)).append("\n");
                        }
                    else
                        print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_disabled_system"), systemId)).append("\n");
                } catch (Exception e) {
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_system"), systemId));
                    Console.showMessage(print);
                    return CommandResult.ERROR;
                }
            }
        } else {
            // Verify first that arguments only contain valid ids
            for (String systemId : tmp)
                if (!systems.has(systemId)) {
                    Console.showMessage(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_no_system_id"), systemId));
                    return CommandResult.ERROR;
                }

            // Generate only custom star systems with specified ids
            for (String systemId : tmp)
                try {
                    JSONObject systemOptions = systems.getJSONObject(systemId);
                    for (int numOfSystems = systemOptions.optInt(util.OPT_NUMBER_OF_SYSTEMS, 1); numOfSystems > 0; numOfSystems--) {
                        util.generateCustomStarSystem(systemOptions);
                        print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_generated_system"), systemId)).append("\n");
                    }
                } catch (Exception e) {
                    print.append(String.format(Global.getSettings().getString("customizablestarsystems", "commands_error_bad_system"), systemId));
                    Console.showMessage(print);
                    return CommandResult.ERROR;
                }
        }

        Console.showMessage(print);

        HashMap<MarketAPI, String> marketsToOverrideAdmin = util.marketsToOverrideAdmin;
        if (marketsToOverrideAdmin != null) {
            AICoreAdminPluginImpl aiPlugin = new AICoreAdminPluginImpl();
            for (MarketAPI market : marketsToOverrideAdmin.keySet()) {
                String adminType = (String) marketsToOverrideAdmin.get(market);
                if (adminType.equals(Factions.PLAYER)) market.setAdmin(null);
                else if (adminType.equals(Commodities.ALPHA_CORE))
                    market.setAdmin(aiPlugin.createPerson(Commodities.ALPHA_CORE, market.getFaction().getId(), 0));
            }
            // No need for the HashMap afterwards, so clear it just in case
            marketsToOverrideAdmin.clear();
        }

        return CommandResult.SUCCESS;
    }
}