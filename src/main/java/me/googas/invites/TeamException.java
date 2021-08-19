package me.googas.invites;

import lombok.NonNull;
import me.googas.commands.exceptions.type.StarboxException;

public class TeamException extends StarboxException {


    public TeamException(@NonNull String message, @NonNull Throwable cause) {
        super(message, cause);
    }
}
