/**
 * Created by Kay Lerch (https://twitter.com/KayLerch)
 *
 * Contribute to https://github.com/KayLerch/alexa-skills-kit-tellask-java
 *
 * Attached license applies.
 * This source is licensed under GNU GENERAL PUBLIC LICENSE Version 3 as of 29 June 2007
 */
package handler;

import io.klerch.alexa.tellask.model.AlexaInput;
import io.klerch.alexa.tellask.model.AlexaOutput;
import io.klerch.alexa.tellask.schema.AlexaIntentHandler;
import io.klerch.alexa.tellask.schema.annotation.AlexaIntentListener;
import io.klerch.alexa.tellask.schema.type.AlexaIntentType;
import io.klerch.alexa.tellask.util.AlexaRequestHandlerException;

@AlexaIntentListener(builtInIntents = {AlexaIntentType.INTENT_CANCEL, AlexaIntentType.INTENT_HELP}, customIntents = { "IntentWithOneUtteranceAndOneReprompt", "test" }, priority = 500)
public class ValidIntentHandler implements AlexaIntentHandler {
    @Override
    public boolean verify(AlexaInput alexaInput) {
        return true;
    }

    @Override
    public AlexaOutput handleRequest(final AlexaInput alexaInput) throws AlexaRequestHandlerException {
        return AlexaOutput.ask("Plus").build();
    }

    @Override
    public AlexaOutput handleError(AlexaRequestHandlerException exception) {
        return AlexaOutput.tell("SaySorry").build();
    }
}
