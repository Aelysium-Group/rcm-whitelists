package group.aelysium.rustyconnector.modules.whitelists.events;

import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.modules.whitelists.RCMWhitelists;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.proxy.events.ServerPreJoinEvent;
import group.aelysium.rustyconnector.proxy.family.Server;

public class OnServerPreConnect {
    @EventListener
    public static void handle(ServerPreJoinEvent event) throws Exception {
        Server server = event.server();
        String name = (String) server.metadata("whitelist").orElse(null);
        if(name == null) return;

        RCMWhitelists.fetch().executeNow(registry -> {
            Particle.Flux<? extends Whitelist> flux = registry.fetch(name).orElse(null);
            if(flux == null) return;

            flux.executeNow(whitelist -> {
                if(whitelist.shouldAllow(event.player())) return;
                event.canceled(true, whitelist.denyMessage());
            });
        });
    }
}
