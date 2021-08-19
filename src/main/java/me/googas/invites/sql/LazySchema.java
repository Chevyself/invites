package me.googas.invites.sql;

import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.io.context.PropertiesContext;

import java.net.URL;
import java.util.Objects;
import java.util.Properties;

@Deprecated
public class LazySchema {

    @NonNull
    private final static PropertiesContext CONTEXT = new PropertiesContext();
    @NonNull
    private final Properties properties;

    public LazySchema(@NonNull Properties properties) {
        this.properties = properties;
    }

    @NonNull
    public String getSql(@NonNull String key) {
        return Objects.requireNonNull(this.properties.getProperty(key));
    }

    @NonNull
    public static LazySchema of(@NonNull URL resource) {
        return new LazySchema(LazySchema.CONTEXT.read(resource).handle(e -> Invites.handle(e, () -> "Could not load schema for: " + resource)).provide().orElseGet(Properties::new));
    }

}
