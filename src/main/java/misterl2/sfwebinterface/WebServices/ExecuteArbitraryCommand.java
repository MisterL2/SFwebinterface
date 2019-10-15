package misterl2.sfwebinterface.WebServices;

import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;

import java.io.IOException;

public class ExecuteArbitraryCommand extends WebServiceBase {

    public ExecuteArbitraryCommand(SFwebinterface plugin, Logger logger, String password) {
        super(plugin, logger, password);
    }

    @Override
    public void handleAuthenticatedRequest() throws IOException {
        //THIS IS HIGHLY DANGEROUS IF IN THE WRONG HANDS. MAKE SURE YOU KNOW WHAT YOU ARE DOING IF YOU ACTIVATE THIS!
        String command = getMandatoryGETParam("command");
        logger.info("Executing command \"" + command + "\" due to a webinterface request!");
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    }
}
