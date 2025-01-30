package group.aelysium.rustyconnector.modules.whitelists.lib;

import group.aelysium.rustyconnector.common.modules.ModuleTinder;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.proxy.Permission;
import group.aelysium.rustyconnector.proxy.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides player validation services.
 */
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
        usernames.forEach(s->this.usernames.add(s.toLowerCase()));
        this.permission = permission;
        this.invert = inverted;
        this.denyMessage = denyMessage;
    }

    /**
     * @return The name of this whitelist.
     */
    public @NotNull String name() {
        return this.name;
    }

    /**
     * @return An immutable Set containing the UUIDs targeted by this whitelist.
     */
    public @NotNull Set<UUID> uuids() {
        return Collections.unmodifiableSet(this.uuids);
    }

    /**
     * @return An immutable Set containing the usernames targeted by this whitelist.
     */
    public @NotNull Set<String> usernames() {
        return Collections.unmodifiableSet(this.usernames);
    }

    /**
     * Gets whether the bypass permission is enabled for this whitelist.
     * If enabled, {@link Permission#validate(Player, String...)} will be used to validate that the player has the permission returned by {@link #permission()}.
     * @return `true` is a permission can be used to bypass this whitelist. `false` otherwise.
     */
    public boolean permissionEnabled() {
        return this.permission;
    }

    /**
     * @return The string permission that the player must have to bypass this whitelist.
     */
    public @Nullable String permission() {
        if(!this.permission) return null;
        return "rustyconnector.whitelist."+this.name;
    }

    /**
     * @return The message this will be returned if the player fails the whitelist check.
     */
    public @NotNull String denyMessage() {
        return this.denyMessage;
    }

    /**
     * @return `true` if this whitelist is inverted and should operate as a blacklist. `false` otherwise.
     */
    public boolean isBlacklist() {
        return this.invert;
    }

    /**
     * Validates the player based on the whitelist's multiple parameters.
     * @param player The player to validate.
     * @return `true` if the player has passed the validation and should be allowed to continue. `false` otherwise.
     */
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

    public static class Tinder extends ModuleTinder<Whitelist> {
        private final String name;
        private final Set<UUID> uuids = new HashSet<>();
        private final Set<String> usernames = new HashSet<>();
        private String kickMessage = "You aren't whitelisted on this server.";
        private boolean permission = false;
        private boolean inverted = false;

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
            this.inverted = true;
        }
        public void makeWhitelist() {
            this.inverted = false;
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
                    this.inverted,
                    this.kickMessage
            );
        }
    }
}
