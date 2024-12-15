package group.aelysium.rustyconnector.modules.whitelists.lib;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.plugins.PluginHolder;
import group.aelysium.rustyconnector.common.plugins.PluginTinder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WhitelistRegistry implements Particle, PluginHolder {
    protected final Map<String, Flux<? extends Whitelist>> whitelists = new ConcurrentHashMap<>();
    protected final AtomicReference<Flux<? extends Whitelist>> proxyWhitelist = new AtomicReference<>();

    protected WhitelistRegistry() {}

    public void assignToProxy(@NotNull Flux<? extends Whitelist> whitelist) throws NoSuchElementException, IllegalArgumentException {
        String name = whitelist.metadata("name");
        if(name == null) throw new IllegalArgumentException("The provided flux must have the `name`, `description`, and `details` metadata added.");
        if(this.whitelists.containsKey(name)) throw new NoSuchElementException("No whitelist with the name "+name+" has been registered.");
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
        this.whitelists.remove(whitelistName);
        if(this.proxyWhitelist.get() != null && Objects.equals(this.proxyWhitelist.get().metadata("name"), whitelistName))
            this.proxyWhitelist.set(null);
    }

    public void register(@NotNull Flux<? extends Whitelist> whitelist) throws Exception {
        String name = whitelist.metadata("name");
        if(name == null) throw new IllegalArgumentException("The provided flux must have the `name`, `description`, and `details` metadata added.");
        if(this.whitelists.containsKey(name)) throw new IllegalArgumentException("A whitelist with the name "+name+" already exist.");
        whitelist.observe();
        this.whitelists.put(name, whitelist);
    }

    public Optional<Flux<? extends Whitelist>> proxyWhitelist() {
        return Optional.ofNullable(this.proxyWhitelist.get());
    }

    public @NotNull Optional<Flux<? extends Whitelist>> fetch(@NotNull String whitelistName) {
        try {
            return Optional.ofNullable(this.whitelists.get(whitelistName));
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    @Override
    public void close() throws Exception {
        this.proxyWhitelist.set(null);
        this.whitelists.forEach((k, v)->v.close());
        this.whitelists.clear();
    }

    public @NotNull Map<String, Flux<? extends Whitelist>> whitelists() {
        return Collections.unmodifiableMap(this.whitelists);
    }

    @Override
    public Map<String, Flux<? extends Particle>> plugins() {
        return Collections.unmodifiableMap(this.whitelists);
    }

    public static class Tinder extends PluginTinder<WhitelistRegistry> {
        public Tinder() {
            super(
                    "WhitelistRegistry",
                    "Provides whitelist functionality for the proxy and families.",
                    "rustyconnector-whitelist-whitelistRegistryDetails"
            );
        }

        @Override
        public @NotNull WhitelistRegistry ignite() throws Exception {
            return new WhitelistRegistry();
        }
    }
}
