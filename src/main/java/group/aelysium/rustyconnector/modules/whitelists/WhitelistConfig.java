package group.aelysium.rustyconnector.modules.whitelists;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.declarative_yaml.lib.Printer;
import group.aelysium.rustyconnector.common.modules.ModuleTinder;
import group.aelysium.rustyconnector.modules.whitelists.lib.Whitelist;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Namespace("rustyconnector-modules")
@Config("/rcm-whitelists/{name}.yml")
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                        Whitelist                         #",
        "#                                                          #",
        "#               ---------------------------                #",
        "#                                                          #",
        "# | Setup your whitelist! The name of this whitelist       #",
        "# | is the same as the name you give this file!            #",
        "#                                                          #",
        "# | To make a new whitelist, just duplicate this           #",
        "# | template, rename it, and configure it how you'd like!  #",
        "#                                                          #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################",
})
public class WhitelistConfig {
    @PathParameter("name")
    private String name;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                           UUID                           #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | The players whitelist allows three parameters to give  #",
            "# | your criteria more or less flexibility!                #",
            "# | Username, uuid, and IP Address                         #",
            "#                                                          #",
            "# NOTE: You'll want to make sure that you use a UUID       #",
            "#       format containing dashes! If you use the format    #",
            "#       without dashes the whitelist will fail to load!    #",
            "#                                                          #",
            "#       Example:                                           #",
            "#       00000000-0000-0000-0000-000000000000               #",
            "#                                                          #",
            "#       Example (INVALID):                                 #",
            "#       00000000000000000000000000000000                   #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
    })
    @Node(1)
    private List<String> uuids = List.of();

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                         Username                         #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | A list of usernames to target.                         #",
            "# | Usernames are not case-sensitive.                      #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
    })
    @Node(2)
    private List<String> usernames = List.of();

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                       Permission                         #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | If you'd only like players with a certain permission   #",
            "# | to clear this whitelist, enable this value!            #",
            "#                                                          #",
            "#   rustyconnector.whitelist.<whitelist name >             #",
            "# | Gives a player permission to pass the                  #",
            "# | specific whitelist.                                    #",
            "#                                                          #",
            "#   rustyconnector.whitelist.*                             #",
            "# | Gives a player permission to pass all whitelists.      #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
    })
    @Node(3)
    private boolean usePermission = false;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                      Kick Message                        #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | The message to show a player if they                   #",
            "# | fail the whitelist.                                    #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
    })
    @Node(4)
    private String kickMessage = "You aren't whitelisted on this server.";

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                        Inverted                          #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | Inverting a whitelist will cause it to operate as      #",
            "# | a blacklist.                                           #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
    })
    @Node(5)
    private boolean inverted = false;

    public @NotNull ModuleTinder<? extends Whitelist> tinder() {
        Whitelist.Tinder tinder = new Whitelist.Tinder(this.name);

        this.uuids.forEach(s -> {
            try {
                UUID u = UUID.fromString(s);
                tinder.addUUID(u);
            } catch (Exception ignore) {}
        });
        this.usernames.forEach(tinder::addUsername);

        tinder.kickMessage(this.kickMessage);
        if(this.inverted) tinder.makeBlacklist();
        else tinder.makeWhitelist();

        return tinder;
    }

    public static WhitelistConfig New(@NotNull String name) {
        return DeclarativeYAML.From(
                WhitelistConfig.class,
                new Printer().pathReplacements(Map.of(
                        "name", name
                ))
        );
    }
}