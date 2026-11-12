package org.tranquility.customizablestarsystems.commands;

import org.json.JSONException;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

import java.io.IOException;

import static org.tranquility.customizablestarsystems.CSSStrings.COMMANDS_ERROR_BAD_JSON;
import static org.tranquility.customizablestarsystems.CSSUtil.getCustomStarSystemIds;
import static org.tranquility.customizablestarsystems.CSSUtil.getMergedSystemJSON;

public class ListCustomStarSystems implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        try {
            getMergedSystemJSON();
        } catch (JSONException | IOException e) {
            Console.showMessage(COMMANDS_ERROR_BAD_JSON + e);
            return CommandResult.ERROR;
        }

        StringBuilder print = new StringBuilder();
        for (String systemId : getCustomStarSystemIds())
            print.append(systemId).append("\n");
        Console.showMessage(print);

        return CommandResult.SUCCESS;
    }
}