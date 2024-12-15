package group.aelysium.rustyconnector.modules.whitelists.events;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.modules.whitelists.RCMWhitelists;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.proxy.events.NetworkPreJoinEvent;

public class OnProxyPreConnect {
    @EventListener
    public static void handle(NetworkPreJoinEvent event) throws Exception {
        RCMWhitelists.fetch().executeNow(registry -> {
            Particle.Flux<? extends Whitelist> flux = registry.proxyWhitelist().orElse(null);
            if(flux == null) return;

            flux.executeNow(whitelist -> {
                if(whitelist.shouldAllow(event.player())) return;
                event.canceled(true, whitelist.denyMessage());
            });
        });
    }
}
