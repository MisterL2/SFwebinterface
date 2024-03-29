package misterl2.sfwebinterface;

import com.google.inject.Inject;
import misterl2.sfwebinterface.WebServices.*;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ValueType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

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

    @Inject
    @DefaultConfig(sharedRoot = true)
    private Path configDir;

    private Path configFile = Paths.get( "config/sfwebinterface.conf");
    private ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(configFile).build();

    private int port = 20512;

    //This is not safe and should be changed immediately! But in case the server admin forgets, this is still better than "admin"
    private String password = "tHiScOuLdBeAsAfEPaSsWoRd" + new Random().nextInt(100000000); //Autogenerated default value

    private boolean executeArbitrary = false;
    private boolean configLoadedCorrectly = false; //For security reasons, don't allow the web interface to do anything without proper config setup!

    @Listener
    public void init(GameInitializationEvent event) {
        logger.info("SFUtilities loading...");
        logger.info("Loading config...");
        ConfigurationNode rootNode = null;
        try {
            rootNode = configLoader.load();
            List<? extends ConfigurationNode> childrenList = rootNode.getChildrenList();
            if(rootNode.getValueType() == ValueType.NULL) { //Creates config, if it doesn't exist yet
                logger.info("No config found, creating new config");
                ConfigurationNode portNode = rootNode.getNode("port");
                portNode.setValue(20512);

                ConfigurationNode passwordNode = rootNode.getNode("password");
                passwordNode.setValue(password);
                logger.warn("Using an autogenerated default password is not really safe! Set your own password in the config!");

                ConfigurationNode executeArbitraryNode = rootNode.getNode("execute-any-command"); //Very dangerous
                executeArbitraryNode.setValue(false);

                //System.out.println(configLoader.canSave());
                configLoader.save(rootNode);
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        readConfig(rootNode);
        port = (Integer) rootNode.getNode("port").getValue();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) throws IOException {
        if(!configLoadedCorrectly) {
            logger.error("WebInterface config not set up correctly, so web service will NOT be started! Set up the config and set a password first!");
            return;
        }

        logger.info("Starting WEB SERVICE!");
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/playerlist", new GetPlayerList(this,logger, password));
        server.createContext("/kick", new KickPlayer(this,logger, password));
        server.createContext("/ban", new BanPlayer(this,logger, password));
        server.createContext("/pardon", new PardonPlayer(this,  logger, password));
        if(executeArbitrary) {
            //THIS IS HIGHLY DANGEROUS IF IN THE WRONG HANDS. MAKE SURE YOU KNOW WHAT YOU ARE DOING IF YOU ACTIVATE THIS!
            logger.warn("Execution of arbitrary console commands has been ENABLED! This is potentially a dangerous security risk, so only enable it (in the config) if you really know what you are doing!");
            server.createContext("/console", new ExecuteArbitraryCommand(this,  logger, password));
        }
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    private void readConfig(ConfigurationNode rootNode) {
        if(rootNode == null) {
            logger.error("Config could not be loaded!");
            return;
        }

        if(rootNode.getNode("password").getValue() == null) {
            logger.error("There is no password in the config file! The Web Service will NOT start without a password!");
            return;
        }

        port = (Integer) rootNode.getNode("port").getValue(20512);
        password = rootNode.getNode("password").getValue().toString();
        executeArbitrary = rootNode.getNode("execute-any-command").getBoolean(false);

        configLoadedCorrectly = true;
        logger.info("Config was loaded!");
    }




}
