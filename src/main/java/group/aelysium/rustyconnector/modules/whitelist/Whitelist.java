package group.aelysium.rustyconnector.modules.whitelist;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.modules.Module;
import group.aelysium.rustyconnector.proxy.Permission;
import group.aelysium.rustyconnector.proxy.player.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

/**
 * Provides player validation services.
 */
public class Whitelist implements Module {
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
    
    @Override
    public @Nullable Component details() {
        return join(
            JoinConfiguration.newlines(),
            RC.Lang("rustyconnector-keyValue").generate("Name", this.name),
            RC.Lang("rustyconnector-keyValue").generate("Is Blacklist", this.invert),
            RC.Lang("rustyconnector-keyValue").generate("Deny Message", this.denyMessage),
            RC.Lang("rustyconnector-keyValue").generate("Bypass Permission", this.permission ? this.permission() : text("Disabled", DARK_GRAY)),
            RC.Lang("rustyconnector-keyValue").generate("UUIDs", String.join(", ", this.uuids.stream().map(UUID::toString).toList())),
            RC.Lang("rustyconnector-keyValue").generate("Usernames", String.join(", ", this.usernames.stream().toList()))
        );
    }
}
