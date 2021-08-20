package me.googas.invites.sql;

import lombok.Getter;
import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.io.context.PropertiesContext;
import me.googas.lazy.sql.LazySQLSubloader;
import me.googas.lazy.sql.SQLElement;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class LazySchema {

    @NonNull
    private final static PropertiesContext CONTEXT = new PropertiesContext();
    @NonNull
    private final Properties properties;
    @NonNull @Getter
    private final Type type;

    public LazySchema(@NonNull Properties properties, @NonNull Type type) {
        this.properties = properties;
        this.type = type;
    }

    @NonNull
    public String getSql(@NonNull String key) {
        return Objects.requireNonNull(this.properties.getProperty(key));
    }

     public void updateId(@NonNull LazySQLSubloader subloader, @NonNull PreparedStatement statement, @NonNull SQLElement element) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            if (this.type == LazySchema.Type.SQL) {
                element.setId(resultSet.getInt("id"));
            } else {
                ResultSet keysResult = subloader.statementOf("SELECT last_insert_rowid()").executeQuery();
                if (keysResult.next()) {
                    element.setId(resultSet.getInt(1));
                }
            }
        }
    }

    @NonNull
    public static LazySchema of(@NonNull URL resource, @NonNull Type type) {
        return new LazySchema(LazySchema.CONTEXT.read(resource).handle(e -> Invites.handle(e, () -> "Could not load schema for: " + resource)).provide().orElseGet(Properties::new), type);
    }

    @NonNull
    public static LazySchema of(@NonNull ClassLoader loader, @NonNull Type type) {
        return LazySchema.of(LazySchema.class.getClassLoader().getResource("schemas/" + type.toString().toLowerCase() + ".properties"), type);
    }

    public enum Type {
        SQLITE,
        SQL
    }
}
