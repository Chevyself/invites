package me.googas.invites.commands;

import me.googas.commands.bukkit.annotations.Command;
import me.googas.commands.bukkit.result.Result;

public class InvitationsCommand {

    @Command(aliases = "accept", description = "Accept an invitation to join a team", permission = "invites.accept")
    public Result accept() {
        return new Result();
    }

    @Command(aliases = "deny", description = "Deny an invitation to join a team", permission = "invites.deny")
    public Result deny() {
        return new Result();
    }

}
