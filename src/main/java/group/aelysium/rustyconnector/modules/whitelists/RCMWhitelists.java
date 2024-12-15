package group.aelysium.rustyconnector.modules.whitelists;

import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.RustyConnector;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.modules.BuildConstants;
import group.aelysium.rustyconnector.modules.whitelists.configs.WhitelistConfig;
import group.aelysium.rustyconnector.modules.whitelists.events.OnFamilyPreConnect;
import group.aelysium.rustyconnector.modules.whitelists.events.OnProxyPreConnect;
import group.aelysium.rustyconnector.modules.whitelists.events.OnServerPreConnect;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.modules.whitelists.lib.WhitelistLang;
import group.aelysium.rustyconnector.modules.whitelists.lib.WhitelistRegistry;
import group.aelysium.rustyconnector.proxy.util.Version;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Objects;

@Plugin(
        id = "rcm-whitelists",
        name = "rcm-Whitelists",
        version = BuildConstants.VERSION,
        description = "A whitelist implementation for RustyConnector Velocity.",
        url = "https://aelysium.group/",
        authors = {"Juice"},
        dependencies = {
                @Dependency(id = "rustyconnector-velocity")
        }
)
public class RCMWhitelists {
    private static final Particle.Flux<? extends WhitelistRegistry> instance = (new WhitelistRegistry.Tinder()).flux();
    public static Particle.Flux<? extends WhitelistRegistry> fetch() {
        return instance;
    }

    @Subscribe
    public void onStart(ProxyInitializeEvent event) {
        try {
            instance.onStart(r -> {
                try {
                    WhitelistConfig.New("default");
                    for (File file : Objects.requireNonNull((new File("plugins/rcm-whitelists/")).listFiles())) {
                        if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                        int extensionIndex = file.getName().lastIndexOf(".");
                        String name = file.getName().substring(0, extensionIndex);
                        Whitelist.Tinder tinder = WhitelistConfig.New(name).tinder();
                        Particle.Flux<? extends Whitelist> whitelist = tinder.flux();
                        whitelist.observe();
                        r.register(whitelist);
                    }
                } catch (Exception e) {
                    RC.Error(Error.from(e).whileAttempting("To boot up the FamilyRegistry."));
                }
            });
            RustyConnector.Kernel(kernelFlux -> kernelFlux.onStart(kernel -> {
                if (!kernel.version().equals(Version.create(0, 9, 0)))
                    RC.Adapter().log(Component.text("Attempting to run "));

                try {
                    instance.observe();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                try {
                    kernel.fetchPlugin("EventManager").onStart(e -> {
                        ((EventManager) e).listen(OnFamilyPreConnect.class);
                        ((EventManager) e).listen(OnProxyPreConnect.class);
                        ((EventManager) e).listen(OnServerPreConnect.class);
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                try {
                    kernel.fetchPlugin("LangLibrary").onStart(lang -> {
                        ((LangLibrary) lang).registerLangNodes(WhitelistLang.class);
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                kernel.registerPlugin(instance);
            }));
        } catch (Exception e) {
            RC.Error(Error.from(e).whileAttempting("To start up the RCM-Whitelists module."));
        }
    }

    @Subscribe
    public void onClose(ProxyShutdownEvent event) {
        instance.close();
    }
}
