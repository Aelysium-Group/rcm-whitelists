package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.declarative_yaml.lib.Printer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@Namespace("rustyconnector")
@Config("/config.yml")
class DefaultConfig {
    @Node
    private String proxyWhitelist = null;

    public @Nullable String proxyWhitelist() {
        if(this.proxyWhitelist == null) return null;
        return this.proxyWhitelist.isBlank() ? null : this.proxyWhitelist;
    }

    public static DefaultConfig ReadFrom() throws IOException {
        DefaultConfig config = new DefaultConfig();
        try {
            DeclarativeYAML.ReadOnly(config, new Printer());
        } catch (Exception ignore) {}
        return config;
    }
}