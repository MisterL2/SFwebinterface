package misterl2.sfwebinterface;

import com.google.inject.Inject;
import misterl2.sfwebinterface.WebServices.BanPlayer;
import misterl2.sfwebinterface.WebServices.GetPlayerList;
import misterl2.sfwebinterface.WebServices.KickPlayer;
import misterl2.sfwebinterface.WebServices.PardonPlayer;
import org.slf4j.Logger;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;
import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;

@Plugin(
        id = "sfwebinterface",
        name = "SFwebinterface",
        description = "Allows other applications to connect to the running server and run commands via HTTP requests to connect e.g. to a discord bot",
        authors = {
                "MisterL2"
        }
)
public class SFwebinterface {

    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        System.out.println("Starting WEB SERVICE!");
        HttpServer server = HttpServer.create(new InetSocketAddress(20512), 0);
        server.createContext("/playerlist", new GetPlayerList(this,logger));
        server.createContext("/kick", new KickPlayer(this,logger));
        server.createContext("/ban", new BanPlayer(this,logger));
        server.createContext("/pardon", new PardonPlayer(this,  logger));
        server.setExecutor(null); // creates a default executor
        server.start();
    }
}
