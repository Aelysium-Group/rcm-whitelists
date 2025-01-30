package group.aelysium.rustyconnector.modules.whitelists.lib;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.common.modules.ExternalModuleTinder;
import group.aelysium.rustyconnector.common.modules.ModuleCollection;
import group.aelysium.rustyconnector.common.modules.ModuleTinder;
import group.aelysium.rustyconnector.modules.whitelists.*;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.modules.ModuleHolder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class WhitelistRegistry implements Particle, ModuleHolder {
    protected final ModuleCollection whitelists = new ModuleCollection();
    protected final AtomicReference<Flux<? extends Whitelist>> proxyWhitelist = new AtomicReference<>();

    protected WhitelistRegistry(
            List<? extends ModuleTinder<? extends Whitelist>> whitelists
    ) {
        whitelists.forEach(w-> {
            try {
                this.whitelists.registerModule(w);
            } catch (Exception e) {
                RC.Error(Error.from(e).whileAttempting("To startup the RCM-WhitelistRegistry module."));
            }
        });
    }

    public void assignToProxy(@NotNull Flux<? extends Whitelist> whitelist) throws NoSuchElementException, IllegalArgumentException {
        String name = whitelist.metadata("name");
        if(name == null) throw new IllegalArgumentException("The provided flux must have the `name`, `description`, and `details` metadata added.");
        if(this.whitelists.containsModule(name)) throw new NoSuchElementException("No whitelist with the name "+name+" has been registered.");
        this.proxyWhitelist.set(whitelist);
    }

    public @NotNull Optional<Flux<? extends Whitelist>> proxy() {
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

    public void register(@NotNull ModuleTinder<? extends Whitelist> whitelist) throws Exception {
        Flux<?> flux = whitelist.flux();
        String name = flux.metadata("name");
        if(name == null) throw new IllegalArgumentException("The provided flux must have the `name`, `description`, and `details` metadata added.");
        if(this.whitelists.containsModule(name)) throw new IllegalArgumentException("A whitelist with the name "+name+" already exist.");
        this.whitelists.registerModule(name, whitelist);
    }

    public Optional<Flux<? extends Whitelist>> proxyWhitelist() {
        return Optional.ofNullable(this.proxyWhitelist.get());
    }

    public @NotNull Optional<Flux<? extends Whitelist>> fetch(@NotNull String whitelistName) {
        try {
            return Optional.ofNullable(this.whitelists.fetchModule(whitelistName));
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    @Override
    public void close() throws Exception {
        this.proxyWhitelist.set(null);
        this.whitelists.close();
    }

    public @NotNull Map<String, Flux<? extends Whitelist>> whitelists() {
        Map<String, Flux<? extends Whitelist>> whitelists = new HashMap<>();
        this.whitelists.modules().forEach((k,v)->whitelists.put(k, (Flux<? extends Whitelist>) v));
        return whitelists;
    }

    @Override
    public Map<String, Flux<? extends Particle>> modules() {
        return Collections.unmodifiableMap(this.whitelists.modules());
    }

    public static class Tinder extends ExternalModuleTinder<WhitelistRegistry> {
        private final List<ModuleTinder<? extends Whitelist>> whitelists = new ArrayList<>();

        public Tinder() {
            try {
                WhitelistConfig.New("default");
                File directory = new File("rc-modules/rcm-whitelists");
                if(!directory.exists()) directory.mkdirs();
                for (File file : Optional.ofNullable(directory.listFiles()).orElse(new File[0])) {
                    if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                    int extensionIndex = file.getName().lastIndexOf(".");
                    String name = file.getName().substring(0, extensionIndex);
                    whitelists.add(WhitelistConfig.New(name).tinder());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void bind(@NotNull ProxyKernel kernel) {
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
        public WhitelistRegistry onStart() throws Exception {
            try {
                WhitelistConfig.New("default");
                File directory = new File("rc-modules/rcm-whitelists");
                if(!directory.exists()) directory.mkdirs();
                for (File file : Optional.ofNullable(directory.listFiles()).orElse(new File[0])) {
                    if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                    int extensionIndex = file.getName().lastIndexOf(".");
                    String name = file.getName().substring(0, extensionIndex);
                    ModuleTinder<? extends Whitelist> tinder = WhitelistConfig.New(name).tinder();
                    this.whitelists.add(tinder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new WhitelistRegistry(whitelists);
        }
    }
}
