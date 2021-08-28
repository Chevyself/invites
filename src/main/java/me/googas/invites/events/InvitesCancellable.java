package me.googas.invites.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/** Extension of {@link Cancellable} for Starbox events */
public interface InvitesCancellable extends Cancellable {

  /**
   * Get if the match was cancelled
   *
   * @return true if the match was cancelled
   */
  default boolean get() {
    if (this instanceof Event) {
      Bukkit.getServer().getPluginManager().callEvent((Event) this);
      return this.isCancelled();
    } else {
      throw new UnsupportedOperationException(this + " does not extend " + Event.class);
    }
  }

  /**
   * Get if the match was not cancelled
   *
   * @return true if the match was not cancelled
   */
  default boolean not() {
    return !this.get();
  }
}