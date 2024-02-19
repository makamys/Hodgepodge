package com.mitchej123.hodgepodge.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.mitchej123.hodgepodge.util.AnchorAlarm;

public class DebugCommand extends CommandBase {

    @Override
    public String getName() {
        return "hp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Usage: hp <subcommand>. Valid subcommands are: toggle, anchor, randomNbt.";
    }

    private void printHelp(ICommandSender sender) {
        sender.sendMessage(new ChatComponentText("Usage: hp <toggle|anchor|randomNbt>"));
        sender.sendMessage(new ChatComponentText("\"toggle anchordebug\" - toggles RC anchor debugging"));
        sender.sendMessage(
                new ChatComponentText(
                        "\"anchor list [player]\" - list RC anchors placed by the player (empty for current player)"));
        sender.sendMessage(
                new ChatComponentText(
                        "\"randomNbt [bytes]\" - adds a random byte array of the given size to the held item"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List getSuggestions(ICommandSender sender, String[] ss) {
        List<String> l = new ArrayList<>();
        String test = ss.length == 0 ? "" : ss[0].trim();
        if (ss.length == 0 || ss.length == 1
                && (test.isEmpty() || Stream.of("toggle", "anchor", "randomNbt").anyMatch(s -> s.startsWith(test)))) {
            Stream.of("toggle", "anchor", "randomNbt").filter(s -> test.isEmpty() || s.startsWith(test))
                    .forEach(l::add);
        } else if (test.equals("toggle")) {
            String test1 = ss[1].trim();
            if (test1.isEmpty() || "anchordebug".startsWith(test1)) l.add("anchordebug");
        } else if (test.equals("anchor")) {
            String test1 = ss[1].trim();
            if (test1.isEmpty() || ("list".startsWith(test1) && !"list".equals(test1))) l.add("list");
            else if ("list".equals(test1) && ss.length > 2) {
                l.addAll(suggestMatching(ss, MinecraftServer.getInstance().getPlayerNames()));
            }
        }
        return l;
    }

    @Override
    public void run(ICommandSender sender, String[] strings) {
        if (strings.length < 1) {
            printHelp(sender);
            return;
        }
        switch (strings[0]) {
            case "toggle":
                if (strings.length < 2 || !strings[1].equals("anchordebug")) {
                    printHelp(sender);
                    return;
                }
                AnchorAlarm.AnchorDebug = !AnchorAlarm.AnchorDebug;
                sender.sendMessage(new ChatComponentText("Anchor debugging: " + AnchorAlarm.AnchorDebug));
                break;
            case "anchor":
                if (strings.length < 2 || !strings[1].equals("list")) {
                    printHelp(sender);
                    return;
                }
                String playerName = strings.length > 2 ? strings[2] : sender.getName();
                if (!AnchorAlarm.listSavedAnchors(playerName, sender.getSourceWorld())) sender.sendMessage(
                        new ChatComponentText("No such player entity in the current world : " + playerName));
                else sender.sendMessage(
                        new ChatComponentText("Saved anchors dumped to the log for player: " + playerName));
                break;
            case "randomNbt":
                if (strings.length < 2) {
                    printHelp(sender);
                    return;
                }
                final int byteCount = NumberUtils.toInt(strings[1], -1);
                if (byteCount < 1) {
                    printHelp(sender);
                    return;
                }
                final EntityPlayerMP player = asPlayer(sender);
                if (player.inventory == null) {
                    return;
                }
                final ItemStack stack = player.inventory.getMainHandStack();
                if (stack == null || stack.getItem() == null) {
                    return;
                }
                if (stack.nbt == null) {
                    stack.nbt = new NBTTagCompound();
                }
                final byte[] randomData = RandomUtils.nextBytes(byteCount);
                stack.nbt.putByteArray("DebugJunk", randomData);
                player.inventory.dirty = true;
                player.playerMenu.updateListeners();
                break;
        }
    }
}
