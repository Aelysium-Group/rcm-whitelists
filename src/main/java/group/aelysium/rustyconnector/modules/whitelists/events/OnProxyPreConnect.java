package group.aelysium.rustyconnector.modules.whitelists.events;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.modules.whitelists.WhitelistRegistry;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.common.events.EventPriority;
import group.aelysium.rustyconnector.modules.whitelists.Whitelist;
import group.aelysium.rustyconnector.proxy.events.NetworkPreJoinEvent;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;

public class OnProxyPreConnect {
    @EventListener(order = EventPriority.LOWEST)
    public static void handle(NetworkPreJoinEvent event) {
        RC.Kernel().fetchModule("Whitelists").ifPresent(p -> {
            if(!(p instanceof WhitelistRegistry registry)) return;

            Flux<Whitelist> flux = registry.proxyWhitelist().orElse(null);
            if(flux == null) return;

            flux.ifPresent(whitelist -> {
                if(whitelist.shouldAllow(event.player)) return;
                event.canceled(true, whitelist.denyMessage());
            });
        });
    }
}
