package group.aelysium.rustyconnector.modules.whitelist.events;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.util.Parameter;
import group.aelysium.rustyconnector.modules.whitelist.WhitelistRegistry;
import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.common.events.EventPriority;
import group.aelysium.rustyconnector.modules.whitelist.Whitelist;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.proxy.events.NetworkPreJoinEvent;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Flux;

import java.util.NoSuchElementException;
import java.util.Objects;

public class OnProxyPreConnect {
    @EventListener(order = EventPriority.LOWEST)
    public static void handle(NetworkPreJoinEvent event) {
        try {
            ProxyKernel kernel = RC.P.Kernel();
            
            Parameter proxyWhitelist = kernel.fetchMetadata("whitelist").orElse(null);
            if(proxyWhitelist == null) return;
            
            Flux<WhitelistRegistry> flux = kernel.fetchModule("Whitelists");
            if (flux == null) return;
            if (flux.isEmpty()) return;
            
            WhitelistRegistry registry = flux.orElseThrow();
            
            Flux<Whitelist> whitelistFlux = registry.fetch(proxyWhitelist.getAsString());
            if (whitelistFlux == null) return;
            
            whitelistFlux.ifPresent(whitelist -> {
                if (whitelist.shouldAllow(event.player)) return;
                event.canceled(true, whitelist.denyMessage());
            });
        } catch (Exception e) {
            RC.Error(Error.from(e));
        }
    }
}
