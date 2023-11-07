package data.console.commands;

import com.fs.starfarer.api.Global;
import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import java.util.Iterator;

public class ListCustomStarSystems implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        JSONObject systems;
        try {
            systems = Global.getSettings().getMergedJSONForMod(Global.getSettings().getString("customizablestarsystems", "path_merged_json_customStarSystems"), "customizablestarsystems");
        } catch (Exception e) {
            Console.showMessage(Global.getSettings().getString("customizablestarsystems", "commands_error_badJSON") + e);
            return CommandResult.ERROR;
        }

        StringBuilder print = new StringBuilder();
        for (Iterator<String> it = systems.keys(); it.hasNext(); )
            print.append((String) it.next()).append("\n");
        Console.showMessage(print);

        return CommandResult.SUCCESS;
    }
}