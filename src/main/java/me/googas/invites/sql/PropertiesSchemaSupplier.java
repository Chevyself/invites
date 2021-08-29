package me.googas.invites.sql;

import java.net.URL;
import java.util.Objects;
import java.util.Properties;
import lombok.NonNull;
import me.googas.invites.Invites;
import me.googas.io.context.PropertiesContext;
import me.googas.lazy.sql.LazySchema;

public class PropertiesSchemaSupplier implements LazySchema.SchemaSupplier {

  @NonNull private static final PropertiesContext CONTEXT = new PropertiesContext();
  @NonNull private final Properties properties;

  public PropertiesSchemaSupplier(@NonNull Properties properties) {
    this.properties = properties;
  }

  @NonNull
  public static PropertiesSchemaSupplier of(@NonNull URL resource) {
    return new PropertiesSchemaSupplier(
        PropertiesSchemaSupplier.CONTEXT
            .read(resource)
            .handle(e -> Invites.handle(e, () -> "Could not load schema for: " + resource))
            .provide()
            .orElseGet(Properties::new));
  }

  @Override
  public @NonNull String getSql(@NonNull String key) {
    return Objects.requireNonNull(
        this.properties.getProperty(key), "Could not get schema for " + key);
  }
}
