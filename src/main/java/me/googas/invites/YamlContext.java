package me.googas.invites;

import lombok.NonNull;
import me.googas.io.StarboxFile;
import me.googas.io.context.FileContext;
import me.googas.starbox.expressions.HandledExpression;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

public class YamlContext implements FileContext<YamlConfiguration> {

    @NonNull
    public static final YamlContext INSTANCE = new YamlContext();

    @Override
    public @NonNull <T> HandledExpression<T> read(@NonNull StarboxFile file, @NonNull Class<T> type) {
        return null;
    }

    @Override
    public @NonNull <T> HandledExpression<T> read(@NonNull Reader reader, @NonNull Class<T> type) {
        return null;
    }

    @Override
    public @NonNull HandledExpression<Boolean> write(@NonNull StarboxFile file, @NonNull Object object) {
        return null;
    }

    @Override
    public @NonNull HandledExpression<Boolean> write(@NonNull Writer writer, @NonNull Object object) {
        return null;
    }

    @Override
    public @NonNull <T> HandledExpression<T> read(@NonNull URL resource, @NonNull Class<T> type) {
        return null;
    }

    @Override
    public @NonNull HandledExpression<YamlConfiguration> read(@NonNull StarboxFile file) {
        AtomicReference<Reader> atomicReader = new AtomicReference<>();
        return HandledExpression.using(() -> {
            BufferedReader reader = file.getBufferedReader();
            atomicReader.set(reader);
            return YamlConfiguration.loadConfiguration(reader);
        }).next(() -> {
            Reader reader = atomicReader.get();
            if (reader != null) reader.close();
        });
    }

    @Override
    public @NonNull HandledExpression<YamlConfiguration> read(@NonNull URL resource) {
        AtomicReference<Reader> atomicReader = new AtomicReference<>();
        return HandledExpression.using(() -> {
            InputStreamReader reader = new InputStreamReader(resource.openStream());
            atomicReader.set(reader);
            return YamlConfiguration.loadConfiguration(reader);
        }).next(() -> {
            Reader reader = atomicReader.get();
            if (reader != null) reader.close();
        });
    }
}
