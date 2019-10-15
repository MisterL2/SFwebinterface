package misterl2.sfwebinterface.WebServices;

import misterl2.sfwebinterface.SFwebinterface;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;

public class ExecuteArbitraryCommand extends WebServiceBase {

    public ExecuteArbitraryCommand(SFwebinterface plugin, Logger logger, String password) {
        super(plugin, logger, password);
    }

    @Override
    public void handleAuthenticatedRequest() throws IOException {
        //THIS IS HIGHLY DANGEROUS IF IN THE WRONG HANDS. MAKE SURE YOU KNOW WHAT YOU ARE DOING IF YOU ACTIVATE THIS!
        String userCommand = getMandatoryGETParam("command");
        final String command = userCommand.charAt(0) == '/' ? userCommand.substring(1) : userCommand; //Commands do not use a / when executed by the commandmanager. For convenience, remove the / from any command starting with it.

        logger.info("Executing command \"" + command + "\" due to a webinterface request!");
        Task.builder().execute(
                () -> Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command)
        ).submit(plugin);

        returnResponse(200, "The command has been received and executed on the server!");
    }
}
