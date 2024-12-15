package group.aelysium.rustyconnector.modules.whitelists.lib;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.plugins.PluginTinder;
import group.aelysium.rustyconnector.proxy.Permission;
import group.aelysium.rustyconnector.proxy.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Whitelist implements Particle {
    private final String name;
    private final Set<UUID> uuids = ConcurrentHashMap.newKeySet();
    private final Set<String> usernames = ConcurrentHashMap.newKeySet();
    private final boolean permission;
    private final String denyMessage;
    private final boolean invert;
    protected Whitelist(
            @NotNull String name,
            @NotNull Set<UUID> uuids,
            @NotNull Set<String> usernames,
            boolean permission,
            boolean inverted,
            @NotNull String denyMessage
    ) {
        this.name = name;
        this.uuids.addAll(uuids);
        this.usernames.addAll(usernames);
        this.permission = permission;
        this.invert = inverted;
        this.denyMessage = denyMessage;
    }

    public @NotNull String name() {
        return this.name;
    }
    public @NotNull Set<UUID> uuids() {
        return Collections.unmodifiableSet(this.uuids);
    }
    public @NotNull Set<String> usernames() {
        return Collections.unmodifiableSet(this.usernames);
    }
    public boolean permissionEnabled() {
        return this.permission;
    }
    public @Nullable String permission() {
        if(!this.permission) return null;
        return "rustyconnector.whitelist."+this.name;
    }
    public @NotNull String denyMessage() {
        return this.denyMessage;
    }
    public boolean isBlacklist() {
        return this.invert;
    }

    public boolean shouldAllow(@NotNull Player player) {
        boolean shouldAllow = false;

        if(this.uuids.contains(player.uuid())) shouldAllow = true;
        if(this.usernames.contains(player.username().toLowerCase())) shouldAllow = true;
        if(this.permission && Permission.validate(player, this.permission())) shouldAllow = true;

        if(this.invert) shouldAllow = !shouldAllow;
        return shouldAllow;
    }

    @Override
    public void close() throws Exception {
        this.uuids.clear();
        this.usernames.clear();
    }

    public static class Tinder extends PluginTinder<Whitelist> {
        private final String name;
        private final Set<UUID> uuids = new HashSet<>();
        private final Set<String> usernames = new HashSet<>();
        private String kickMessage = "You aren't whitelisted on this server.";
        private boolean permission = false;
        private boolean invert;

        public Tinder(
                String name
        ) {
            super(
                    name,
                    "Provides player validation for player connection points.",
                    "rustyconnector-whitelist-whitelistDetails"
            );
            this.name = name.replace(" ","_");
        }

        public void addUUID(@NotNull UUID uuid) {
            this.uuids.add(uuid);
        }
        public void addUsername(@NotNull String username) {
            this.usernames.add(username);
        }

        public void kickMessage(@NotNull String message) {
            this.kickMessage = message;
        }

        public void makeBlacklist() {
            this.invert = true;
        }
        public void makeWhitelist() {
            this.invert = false;
        }

        public void enablePermission() {
            this.permission = true;
        }
        public void disablePermission() {
            this.permission = false;
        }

        @Override
        public @NotNull Whitelist ignite() throws Exception {
            return new Whitelist(
                    this.name,
                    this.uuids,
                    this.usernames,
                    this.permission,
                    this.invert,
                    this.kickMessage
            );
        }
    }
}
