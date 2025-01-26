package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.rustyconnector.common.RCKernel;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.proxy.ProxyKernel;
import group.aelysium.rustyconnector.shaded.com.google.code.gson.gson.Gson;
import group.aelysium.rustyconnector.shaded.com.google.code.gson.gson.JsonObject;
import group.aelysium.rustyconnector.shaded.group.aelysium.ara.Particle;
import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import group.aelysium.rustyconnector.modules.whitelists.lib.WhitelistRegistry;
import group.aelysium.rustyconnector.proxy.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RCMWhitelists implements RC.Plugin.Initializer {
    private static final Particle.Flux<? extends WhitelistRegistry> instance = (new WhitelistRegistry.Tinder()).flux();
    public static Particle.Flux<? extends WhitelistRegistry> fetch() {
        return instance;
    }
    public final Version rustyconnectorVersion;

    public RCMWhitelists() {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("metadata.json")) {
            if (input == null) throw new NullPointerException("Unable to initialize version number from jar.");
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(new String(input.readAllBytes()), JsonObject.class);
            this.rustyconnectorVersion = new Version(object.get("rustyconnector_version").getAsString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        instance.onStart(w -> {
            try {
                WhitelistConfig.New("default");
                File directory = new File("rc-modules/rcm-whitelists");
                if(!directory.exists()) directory.mkdirs();
                for (File file : Optional.ofNullable(directory.listFiles()).orElse(new File[0])) {
                    if (!(file.getName().endsWith(".yml") || file.getName().endsWith(".yaml"))) continue;
                    int extensionIndex = file.getName().lastIndexOf(".");
                    String name = file.getName().substring(0, extensionIndex);
                    Whitelist.Tinder tinder = WhitelistConfig.New(name).tinder();
                    Particle.Flux<? extends Whitelist> whitelist = tinder.flux();
                    try {
                        whitelist.observe();
                        w.register(whitelist);
                    } catch (Exception e) {
                        RC.Error(Error.from(e).whileAttempting("To startup the RCM-WhitelistRegistry module."));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onStart(RCKernel<?> k) {
        if(!(k instanceof ProxyKernel kernel)) throw new RuntimeException("RCM-Whitelists must only be run on the proxy.");

        try {
            //if (kernel.version().equals(Version.create(0, 9, 0)))

            try {
                instance.observe(1, TimeUnit.MINUTES);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {
        instance.close();
    }
}
