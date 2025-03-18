package group.aelysium.rustyconnector.modules.whitelist;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.common.modules.ModuleCollection;
import group.aelysium.rustyconnector.modules.whitelist.configs.WhitelistConfig;
import group.aelysium.rustyconnector.modules.whitelist.events.OnFamilyPreConnect;
import group.aelysium.rustyconnector.modules.whitelist.events.OnProxyPreConnect;
import group.aelysium.rustyconnector.modules.whitelist.events.OnServerPreConnect;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.common.modules.ModuleHolder;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.DeclarativeYAML;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

import static net.kyori.adventure.text.Component.join;

public class WhitelistRegistry implements Module, ModuleHolder<Whitelist> {
    private final ModuleCollection<Whitelist> whitelists = new ModuleCollection<>();
    
    public void unregister(@NotNull String whitelistName) {
        this.whitelists.unregisterModule(whitelistName);
    }

    public void register(@NotNull Module.Builder<Whitelist> whitelist) throws Exception {
        this.whitelists.registerModule(whitelist.name, whitelist);
    }

    public @Nullable Flux<Whitelist> fetch(@NotNull String whitelistName) {
        return this.whitelists.fetchModule(whitelistName);
    }

    @Override
    public void close() throws Exception {
        this.whitelists.close();
    }

    public @NotNull Map<String, Flux<Whitelist>> whitelists() {
        return Map.copyOf(this.whitelists.modules());
    }

    @Override
    public Map<String, Flux<Whitelist>> modules() {
        return Collections.unmodifiableMap(this.whitelists.modules());
    }
    
    @Override
    public @Nullable Component details() {
        return join(
            JoinConfiguration.newlines(),
            RC.Lang("rustyconnector-keyValue").generate("Available Whitelists", String.join(", ", this.whitelists.modules().keySet()))
        );
    }
    
    public static class Builder extends ExternalModuleBuilder<WhitelistRegistry> {
        @Override
        public void bind(@NotNull ProxyKernel kernel, @NotNull WhitelistRegistry instance) {
            try {
                kernel.fetchModule("EventManager").onStart(e -> {
                    ((EventManager) e).listen(OnFamilyPreConnect.class);
                    ((EventManager) e).listen(OnProxyPreConnect.class);
                    ((EventManager) e).listen(OnServerPreConnect.class);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        @Override
        public WhitelistRegistry onStart(@NotNull Context context) throws Exception {
            WhitelistRegistry registry = new WhitelistRegistry();
            
            File directory = new File(DeclarativeYAML.basePath("rustyconnector-modules")+"/rcm-whitelists");
            if(!directory.exists()) directory.mkdirs();
            
            {
                File[] files = directory.listFiles();
                if (files == null || files.length == 0)
                    WhitelistConfig.New("default");
            }
            
            File[] files = directory.listFiles();
            if (files == null) return registry;
            
            for (File file : files) {
                if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                int extensionIndex = file.getName().lastIndexOf(".");
                String name = file.getName().substring(0, extensionIndex);
                registry.register(new Module.Builder<>(name, "Provides itemized player connection filtering.") {
                    @Override
                    public Whitelist get() {
                        try {
                            WhitelistConfig config = WhitelistConfig.New(name);
                            
                            return new Whitelist(
                                name,
                                new HashSet<>(config.ids),
                                new HashSet<>(config.usernames),
                                config.usePermission,
                                config.inverted,
                                config.kickMessage
                            );
                        } catch (Exception e) {
                            RC.Error(Error.from(e).whileAttempting("To generate the whitelist "+name));
                        }
                        return null;
                    }
                });
            }
            return registry;
        }
    }
}
