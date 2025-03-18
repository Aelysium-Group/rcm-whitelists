package group.aelysium.rustyconnector.modules.whitelist.events;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.modules.whitelist.WhitelistRegistry;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.common.events.EventPriority;
import group.aelysium.rustyconnector.modules.whitelist.Whitelist;
import group.aelysium.rustyconnector.proxy.events.FamilyPreJoinEvent;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;

public class OnFamilyPreConnect {
    @EventListener(order = EventPriority.LOWEST)
    public static void handle(FamilyPreJoinEvent event) {
        try {
            String name = (String) event.family.fetchMetadata("whitelist").orElse(null);
            System.out.println(name);
            if (name == null) return;
            
            RC.Kernel().fetchModule("Whitelists").ifPresent(p -> {
                if (!(p instanceof WhitelistRegistry registry)) return;
                
                Flux<Whitelist> flux = registry.fetch(name);
                if (flux == null) return;
                
                flux.ifPresent(whitelist -> {
                    if (whitelist.shouldAllow(event.player)) return;
                    event.canceled(true, whitelist.denyMessage());
                });
            });
        } catch (Exception e) {
            RC.Error(Error.from(e));
        }
    }
}
