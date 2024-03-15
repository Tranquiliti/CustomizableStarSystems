package data.console.commands;

import org.json.JSONObject;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.tranquility.customizablestarsystems.CSSUtil;

import java.util.Iterator;

import static org.tranquility.customizablestarsystems.CSSStrings.COMMANDS_ERROR_BAD_JSON;

public class ListCustomStarSystems implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        JSONObject systems;
        try {
            systems = CSSUtil.getMergedSystemJSON();
        } catch (Exception e) {
            Console.showMessage(COMMANDS_ERROR_BAD_JSON + e);
            return CommandResult.ERROR;
        }

        StringBuilder print = new StringBuilder();
        for (Iterator<String> it = systems.keys(); it.hasNext(); )
            print.append((String) it.next()).append("\n");
        Console.showMessage(print);

        return CommandResult.SUCCESS;
    }
}