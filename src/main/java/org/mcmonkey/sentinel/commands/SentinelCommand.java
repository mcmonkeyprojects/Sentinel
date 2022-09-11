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
import org.bukkit.Material;
import org.bukkit.command.*;
import org.mcmonkey.sentinel.SentinelPlugin;
import org.mcmonkey.sentinel.SentinelTrait;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for the Sentinel command.
 */
public class SentinelCommand implements CommandExecutor, TabCompleter {

    /**
     * Output string representing a message color.
     */
    public static final String colorBasic = ChatColor.YELLOW.toString(), colorBad = ChatColor.RED.toString(), colorEmphasis = ChatColor.AQUA.toString();

    /**
     * Output string representing a success prefix.
     */
    public static final String prefixGood = ChatColor.DARK_GREEN + "[Sentinel] " + colorBasic;

    /**
     * Output string representing a failure prefix.
     */
    public static final String prefixBad = ChatColor.DARK_GREEN + "[Sentinel] " + colorBad;

    /**
     * The "/sentinel" command manager.
     */
    public CommandManager manager;

    /**
     * The main instance of this class.
     */
    public static SentinelCommand instance;

    /**
     * Prepares the command handling system.
     */
    public void buildCommandHandler(PluginCommand command) {
        instance = this;
        manager = new CommandManager();
        manager.setInjector(new Injector());
        grabCommandField();
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

    private static Field commandInfoMethodField;

    private void grabCommandField() {
        try {
            Field field = CommandManager.CommandInfo.class.getDeclaredField("method");
            field.setAccessible(true);
            commandInfoMethodField = field;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isSentinelRequired(String command, String modifier) {
        CommandManager.CommandInfo info = manager.getCommand(command, modifier);
        if (info == null) {
            return false;
        }
        try {
            Method method = (Method) commandInfoMethodField.get(info);
            Requirements[] req = method.getDeclaredAnnotationsByType(Requirements.class);
            if (req == null || req.length == 0) {
                return false;
            }
            for (Class<? extends Trait> traitClass : req[0].traits()) {
                if (traitClass.equals(SentinelTrait.class)) {
                    return true;
                }
            }
        }
        catch (IllegalAccessException ex) {
            ex.printStackTrace();
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

    private static List<String> filterForArg(Collection<String> output, String arg1) {
        String low = arg1.toLowerCase();
        return output.stream().filter(s -> s.startsWith(low)).collect(Collectors.toList());
    }

    public static HashSet<String> addTargetTabCompletions = new HashSet<>();
    public static HashSet<String> itemPrefixes = new HashSet<>(Arrays.asList("helditem", "offhand", "equipped", "in_inventory"));
    public static List<String> materialNames = Arrays.stream(Material.values()).map(m -> m.name().toLowerCase()).collect(Collectors.toList());

    static {
        addTargetTabCompletions.addAll(Arrays.asList("player:", "npc:", "entityname:", "group:"));
        addTargetTabCompletions.addAll(Arrays.asList("helditem:", "offhand:", "equipped:", "in_inventory:"));
        addTargetTabCompletions.addAll(Arrays.asList("status:angry", "status:passive"));
        addTargetTabCompletions.addAll(Arrays.asList("event:", "event:pvp", "event:pve", "event:pvnpc", "event:pvsentinel", "event:eve", "event:pv:", "event:ev:", "event:guarded_fight", "event:message:"));
        addTargetTabCompletions.addAll(SentinelPlugin.targetOptions.keySet().stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    /**
     * Handles tab completion for a player or server command.
     */
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (s.equals("sentinel") && strings.length == 2) {
            NPC npc = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (npc != null && npc.hasTrait(SentinelTrait.class)) {
                SentinelTrait sentinel = npc.getOrAddTrait(SentinelTrait.class);
                switch (strings[0].toLowerCase()) {
                    case "addtarget":
                    case "addignore":
                    case "addavoid":
                        String low = strings[1].toLowerCase();
                        int colon = low.indexOf(':');
                        if (colon != -1 && colon + 1 < low.length()) {
                            String prefix = low.substring(0, colon);
                            if (itemPrefixes.contains(prefix)) {
                                List<String> materials = filterForArg(materialNames, low.substring(colon + 1));
                                materials.add("lore:");
                                materials.add("name:");
                                return filterForArg(materials.stream().map(m -> prefix + ":" + m).collect(Collectors.toList()), low);
                            }
                        }
                        return filterForArg(addTargetTabCompletions, low);
                    case "removetarget":
                        return filterForArg(sentinel.allTargets.getTargetRemovableStrings(), strings[1]);
                    case "removeignore":
                        return filterForArg(sentinel.allIgnores.getTargetRemovableStrings(), strings[1]);
                    case "removeavoid":
                        return filterForArg(sentinel.allAvoids.getTargetRemovableStrings(), strings[1]);
                }
            }
        }
        return manager.onTabComplete(commandSender, command, s, strings);
    }
}
