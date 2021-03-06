/*
 * Trivia by MarCarrot, 2020
 */

package me.marcarrots.trivia;

import me.marcarrots.trivia.menu.subMenus.MainMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;

public class TriviaCommand implements CommandExecutor {

    Trivia trivia;
    QuestionHolder questionHolder;
    ImportQuestions importQuestions;

    public TriviaCommand(Trivia trivia, QuestionHolder questionHolder) {
        this.trivia = trivia;
        this.questionHolder = questionHolder;
        importQuestions = new ImportQuestions(trivia);
    }

    private String helpFormat(String commandName, String commandDescription) {
        return String.format(ChatColor.GOLD + "/trivia %1$s" + ChatColor.WHITE + " - " + ChatColor.AQUA + "%2$s", commandName, commandDescription);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            if (commandSender instanceof Player) {
                MainMenu menu = new MainMenu(trivia.getPlayerMenuUtility((Player) commandSender), trivia, questionHolder);
                menu.open();
            }
        } else {
            String prompt = strings[0];
            switch (prompt) {
                case "reload":
                    try {
                        trivia.reloadConfig();
                        trivia.readQuestions();
                        trivia.loadConfig();
                        trivia.loadMessages();
                        commandSender.sendMessage(Lang.RELOAD.format(null));
                    } catch (Exception e) {
                        commandSender.sendMessage(ChatColor.RED + "Failed to reload files");
                        e.printStackTrace();
                    }
                    return false;
                case "stop":
                    if (trivia.getGame() == null) {
                        commandSender.sendMessage(ChatColor.RED + "There is no trivia game in progress.");
                        return false;
                    }
                    trivia.getGame().stop();
                    commandSender.sendMessage(Lang.GAME_HALTED.format(null));
                    return false;
                case "start":
                    if (trivia.getGame() != null) {
                        commandSender.sendMessage(ChatColor.RED + "There is already a trivia game in progress.");
                        return false;
                    }
                    trivia.setGame(new Game(trivia, questionHolder));
                    trivia.getGame().setParameters(commandSender);
                    trivia.getGame().start();
                    return false;
                case "skip":
                    if (trivia.getGame() == null) {
                        commandSender.sendMessage(ChatColor.RED + "There is no trivia game in progress.");
                        return false;
                    }
                    if (!trivia.getGame().forceSkipRound()) {
                        commandSender.sendMessage(ChatColor.RED + "Failed to skip round. Reason: Skip request made in-between rounds.");
                    }
                    return false;
                case "version":
                    commandSender.sendMessage("Trivia version: " + trivia.getDescription().getVersion());
                    return false;
                case "import":
                    Question q;
                    try {
                        if (strings.length != 2) {
                            commandSender.sendMessage(String.format("%sUsage: %s/trivia import %s<question number>%s,", ChatColor.DARK_AQUA, ChatColor.GOLD, ChatColor.AQUA, ChatColor.DARK_AQUA));
                            commandSender.sendMessage(String.format("%swhere %s<question number> %sis the id of the question to import.", ChatColor.DARK_AQUA, ChatColor.AQUA, ChatColor.DARK_AQUA));
                            commandSender.sendMessage(String.format("%sGet a list of available questions with their ids at: %s%s", ChatColor.DARK_AQUA, ChatColor.AQUA, "pastebin.com/7cTQznMZ"));
                            return false;
                        } else {
                            q = importQuestions.readFile(Integer.parseInt(strings[1]));
                        }
                    } catch (IOException e) {
                        commandSender.sendMessage("An unexpected IO error has occurred.");
                        e.printStackTrace();
                        return false;
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage(String.format("%sThe ID '%s' is invalid and could not be imported. Find valid ones at: %s%s", ChatColor.RED,  strings[1], ChatColor.DARK_RED, "pastebin.com/7cTQznMZ"));
                        return false;
                    }
                    commandSender.sendMessage(Trivia.border);
                    commandSender.sendMessage(String.format(ChatColor.GREEN + "Successfully imported question #%s!", strings[1]));
                    commandSender.sendMessage(String.format("  %sQuestion:    %s%s", ChatColor.GREEN, ChatColor.DARK_AQUA, q.getQuestionString()));
                    commandSender.sendMessage(String.format("  %sAnswer(s):   %s%s",ChatColor.GREEN, ChatColor.DARK_AQUA, q.getAnswerList().toString()));
                    commandSender.sendMessage(Trivia.border);
                    return false;
                default:
                    commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&m------------&e[ &6Trivia &e]&m------------"));
                    commandSender.sendMessage(helpFormat("", "(Main command) Opens up the in-game menu to start trivia or change settings."));
                    commandSender.sendMessage(helpFormat("start", "Quickly starts up a trivia game."));
                    commandSender.sendMessage(helpFormat("stop", "Stops the current trivia game in progress."));
                    commandSender.sendMessage(helpFormat("skip", "Skips a trivia question during a game."));
                    commandSender.sendMessage(helpFormat("import", "Imports a pre-made question/answer from a given ID to your server."));
                    commandSender.sendMessage(helpFormat("reload", "Reloads all the trivia files."));
                    commandSender.sendMessage(helpFormat("help", "Displays this trivia help menu."));
                    commandSender.sendMessage(Trivia.border);
                    return false;
            }

        }

        return false;
    }


}
