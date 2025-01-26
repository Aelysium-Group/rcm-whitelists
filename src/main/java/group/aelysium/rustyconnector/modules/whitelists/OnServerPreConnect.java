package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.common.events.EventPriority;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.proxy.events.ServerPreJoinEvent;
import group.aelysium.rustyconnector.proxy.family.Server;

public class OnServerPreConnect {
    @EventListener(order = EventPriority.LOWEST)
    public static void handle(ServerPreJoinEvent event) throws Exception {
        System.out.println("CONNECTION CAUGHT. STARTING HANDLER");
        Server server = event.server();
        String name = (String) server.metadata("whitelist").orElse(null);
        if(name == null) return;
        System.out.println("METADATA FOUND, MAPPING WHITELIST");

        RCMWhitelists.fetch().executeNow(registry -> {
            Particle.Flux<? extends Whitelist> flux = registry.fetch(name).orElse(null);
            if(flux == null) return;

            flux.executeNow(whitelist -> {
                System.out.println("WHITELIST MAPPED. CHECKING VALIDATION.");
                if(whitelist.shouldAllow(event.player())) return;
                System.out.println("VALIDATION FAILED. CANCELING EVENT.");
                event.canceled(true, whitelist.denyMessage());
            });
        });
    }
}
