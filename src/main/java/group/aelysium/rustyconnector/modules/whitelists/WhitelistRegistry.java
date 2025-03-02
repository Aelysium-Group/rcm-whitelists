package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.common.modules.ModuleCollection;
import group.aelysium.rustyconnector.modules.whitelists.configs.DefaultConfig;
import group.aelysium.rustyconnector.modules.whitelists.configs.WhitelistConfig;
import group.aelysium.rustyconnector.modules.whitelists.events.OnFamilyPreConnect;
import group.aelysium.rustyconnector.modules.whitelists.events.OnProxyPreConnect;
import group.aelysium.rustyconnector.modules.whitelists.events.OnServerPreConnect;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.common.modules.ModuleHolder;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.DeclarativeYAML;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WhitelistRegistry implements Module, ModuleHolder<Whitelist> {
    protected final ModuleCollection<Whitelist> whitelists = new ModuleCollection<>();
    protected final AtomicReference<Flux<Whitelist>> proxyWhitelist = new AtomicReference<>();

    public void assignToProxy(@NotNull Flux<Whitelist> whitelist) throws NoSuchElementException, IllegalArgumentException {
        String name = whitelist.metadata("name");
        if(name == null) throw new IllegalArgumentException("The provided flux must have the `name`, `description`, and `details` metadata added.");
        if(this.whitelists.containsModule(name)) throw new NoSuchElementException("No whitelist with the name "+name+" has been registered.");
        this.proxyWhitelist.set(whitelist);
    }

    public @NotNull Optional<Flux<Whitelist>> proxy() {
        try {
            return Optional.ofNullable(this.proxyWhitelist.get());
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    public void unsetProxy() {
        this.proxyWhitelist.set(null);
    }

    public void unregister(@NotNull String whitelistName) {
        this.whitelists.unregisterModule(whitelistName);
        if(this.proxyWhitelist.get() != null && Objects.equals(this.proxyWhitelist.get().metadata("name"), whitelistName))
            this.proxyWhitelist.set(null);
    }

    public void register(@NotNull String name, @NotNull Module.Builder<Whitelist> whitelist) throws Exception {
        this.whitelists.registerModule(name, whitelist);
    }

    public @Nullable Flux<Whitelist> proxyWhitelist() {
        return this.proxyWhitelist.get();
    }

    public @Nullable Flux<Whitelist> fetch(@NotNull String whitelistName) {
        return this.whitelists.fetchModule(whitelistName);
    }

    @Override
    public void close() throws Exception {
        this.proxyWhitelist.set(null);
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
        return null;
    }
    
    public static class Builder extends ExternalModuleBuilder<WhitelistRegistry> {
        private final List<Module.Builder<Whitelist>> whitelists = new ArrayList<>();

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

            try {
                kernel.fetchModule("LangLibrary").onStart(lang -> {
                    ((LangLibrary) lang).registerLangNodes(WhitelistLang.class);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @NotNull
        @Override
        public WhitelistRegistry onStart(@NotNull Path dataDirectory) {
            WhitelistRegistry registry = new WhitelistRegistry();
            
            try {
                File directory = new File(DeclarativeYAML.basePath("rustyconnector")+"/rcm-whitelists");
                if(!directory.exists()) directory.mkdirs();
                
                {
                    File[] files = directory.listFiles();
                    if (files == null || files.length == 0)
                        DefaultConfig.ReadFrom();
                }
                
                File[] files = directory.listFiles();
                if (files == null) return registry;
                if (files.length == 0) return registry;
                
                for (File file : files) {
                    if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                    int extensionIndex = file.getName().lastIndexOf(".");
                    String name = file.getName().substring(0, extensionIndex);
                     registry.register(name, new Module.Builder<>("Whitelist", ".") {
                        @Override
                        public Whitelist get() {
                            try {
                                WhitelistConfig config = WhitelistConfig.New(name);
                                
                                return new Whitelist(
                                    name,
                                    new HashSet<>(config.uuids.stream().map(UUID::fromString).toList()),
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
            } catch (Exception e) {
                RC.Error(Error.from(e).whileAttempting("To bind StaticFamilyProvider to the FamilyRegistry."));
            }
            
            return registry;
        }
    }
}
