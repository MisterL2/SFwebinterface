package misterl2.sfwebinterface;

import java.io.IOException;

public class HandledInvalidInputException extends IOException { //When the exception has already been dealt with, but a notifier must still be sent in order to avoid returning a misleading null value
}
