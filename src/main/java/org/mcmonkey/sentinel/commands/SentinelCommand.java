package org.mcmonkey.sentinel.commands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.CommandManager;
import net.citizensnpcs.api.command.Injector;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.mcmonkey.sentinel.SentinelTrait;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Helper class for the Sentinel command.
 */
public class SentinelCommand implements CommandExecutor, TabCompleter {

    /**
     * Output string representing a message color.
     */
    public static final String colorBasic = ChatColor.YELLOW.toString();

    /**
     * Output string representing a success prefix.
     */
    public static final String prefixGood = ChatColor.DARK_GREEN + "[Sentinel] " + colorBasic;

    /**
     * Output string representing a failure prefix.
     */
    public static final String prefixBad = ChatColor.DARK_GREEN + "[Sentinel] " + ChatColor.RED;

    /**
     * The "/sentinel" command manager.
     */
    public CommandManager manager;

    /**
     * Prepares the command handling system.
     */
    public void buildCommandHandler(PluginCommand command) {
        manager = new CommandManager();
        manager.setInjector(new Injector());
        grabCommandMethodMap(manager);
        manager.register(SentinelAttackCommands.class);
        manager.register(SentinelChaseCommands.class);
        manager.register(SentinelGreetingCommands.class);
        manager.register(SentinelHealthCommands.class);
        manager.register(SentinelInfoCommands.class);
        manager.register(SentinelIntelligenceCommands.class);
        manager.register(SentinelTargetCommands.class);
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    private static Map<String, Method> sentinelCommandMethodMap;

    private static void grabCommandMethodMap(CommandManager instance) {
        try {
            Field field = CommandManager.class.getDeclaredField("commands");
            field.setAccessible(true);
            sentinelCommandMethodMap = (Map<String, Method>) field.get(instance);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static boolean isSentinelRequired(String command, String modifier) {
        Method method = sentinelCommandMethodMap.get((command + " " + modifier).toLowerCase());
        if (method == null) {
            return false;
        }
        Requirements[] req = method.getDeclaredAnnotationsByType(Requirements.class);
        if (req == null || req.length == 0) {
            return false;
        }
        for (Class<? extends Trait> traitClass : req[0].traits()) {
            if (traitClass.equals(SentinelTrait.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a player or server command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String modifier = args.length > 0 ? args[0] : "";
        if (!manager.hasCommand(command, modifier) && !modifier.isEmpty()) {
            String closest = manager.getClosestCommandModifier(command.getName(), modifier);
            if (!closest.isEmpty()) {
                sender.sendMessage(prefixBad + "Unknown command. Did you mean:");
                sender.sendMessage(prefixGood + " /" + command.getName() + " " + closest);
            }
            else {
                sender.sendMessage(prefixBad + "Unknown command.");
            }
            return true;
        }
        NPC selected = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        SentinelTrait sentinel = null;
        CommandContext context = new CommandContext(sender, args);
        if (context.hasValueFlag("id") && sender.hasPermission("npc.select")) {
            try {
                selected = CitizensAPI.getNPCRegistry().getById(context.getFlagInteger("id"));
            }
            catch (NumberFormatException ex) {
                sender.sendMessage(prefixBad + "ID input is invalid (not an integer)!");
                return true;
            }
        }
        if (selected != null) {
            sentinel = selected.getTraitNullable(SentinelTrait.class);
        }
        Object[] methodArgs;
        if (isSentinelRequired(command.getName(), modifier)) {
            if (sentinel == null) {
                if (selected == null) {
                    sender.sendMessage(prefixBad + "Must have a Sentinel NPC selected!");
                }
                else {
                    sender.sendMessage(prefixBad + "Selected NPC is not a Sentinel! Use /trait sentinel to ensure an NPC becomes a Sentinel.");
                }
                return true;
            }
            if (!selected.getOrAddTrait(Owner.class).isOwnedBy(sender) && !sender.hasPermission("citizens.admin")) {
                sender.sendMessage(prefixBad + "You do not own this NPC (and you are not an admin).");
                return true;
            }
            methodArgs = new Object[]{sender, sentinel};
        }
        else {
            methodArgs = new Object[]{ sender };
        }
        return manager.executeSafe(command, args, sender, methodArgs);
    }

    /**
     * Handles tab completion for a player or server command.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return manager.onTabComplete(commandSender, command, s, strings);
    }
}
