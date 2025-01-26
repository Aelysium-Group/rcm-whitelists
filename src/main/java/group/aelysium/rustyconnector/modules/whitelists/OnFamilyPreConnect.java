package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.common.events.EventPriority;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.proxy.events.FamilyPreJoinEvent;

public class OnFamilyPreConnect {
    @EventListener(order = EventPriority.LOWEST)
    public static void handle(FamilyPreJoinEvent event) {
        event.family().executeNow(family -> {
            String name = (String) family.metadata("whitelist").orElse(null);
            if(name == null) return;

            RCMWhitelists.fetch().executeNow(registry -> {
                Particle.Flux<? extends Whitelist> flux = registry.fetch(name).orElse(null);
                if(flux == null) return;

                flux.executeNow(whitelist -> {
                    if(whitelist.shouldAllow(event.player())) return;
                    event.canceled(true, whitelist.denyMessage());
                });
            });
        });
    }
}
