package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.modules.whitelists.lib.WhitelistRegistry;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.newlines;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

public class WhitelistLang {
    @Lang("rustyconnector-whitelist-whitelistRegistryDetails")
    public static Component whitelistRegistryDetails(WhitelistRegistry registry) {
        Map<String, Particle.Flux<? extends Whitelist>> whitelists = registry.whitelists();

        return join(
                newlines(),
                RC.Lang("rustyconnector-keyValue").generate("Registered Whitelists", (
                        whitelists.isEmpty() ?
                                text("There are no registered whitelists.", DARK_GRAY)
                                :
                                text(String.join(", ", whitelists.entrySet().stream().map(e -> e.getKey()).toList()), BLUE)
                ))
        );
    }

    @Lang("rustyconnector-whitelist-whitelistDetails")
    public static Component whitelistDetails(Whitelist whitelist) {
        return join(
            newlines(),
            RC.Lang("rustyconnector-keyValue").generate("Name", whitelist.name()),
            RC.Lang("rustyconnector-keyValue").generate("Inverted", whitelist.isBlacklist()),
            RC.Lang("rustyconnector-keyValue").generate("Bypass Permission", whitelist.permission() == null ? "Disabled" : whitelist.permission()),
            RC.Lang("rustyconnector-keyValue").generate("Kick Message", whitelist.denyMessage()),
            RC.Lang("rustyconnector-keyValue").generate("Usernames", (
                whitelist.usernames().isEmpty() ?
                    text("There are no targeted usernames.", DARK_GRAY)
                    :
                    text(String.join(", ", whitelist.usernames().stream().toList()), BLUE)
            )),
            RC.Lang("rustyconnector-keyValue").generate("UUIDs", (
                whitelist.uuids().isEmpty() ?
                    text("There are no targeted uuids.", DARK_GRAY)
                    :
                    text(String.join(", ", whitelist.uuids().stream().map(UUID::toString).toList()), BLUE)
            ))
        );
    }
}
