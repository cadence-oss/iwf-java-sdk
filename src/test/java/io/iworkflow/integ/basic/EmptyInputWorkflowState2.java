package io.iworkflow.integ.basic;

import io.iworkflow.core.Context;
import io.iworkflow.core.StateDecision;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.core.command.CommandRequest;
import io.iworkflow.core.command.CommandResults;
import io.iworkflow.core.communication.Communication;
import io.iworkflow.core.persistence.Persistence;

public class EmptyInputWorkflowState2 implements WorkflowState<Void> {

    public static final String StateId = "S2";

    @Override
    public String getStateId() {
        return StateId;
    }

    @Override
    public Class<Void> getInputType() {
        return Void.class;
    }

    @Override
    public CommandRequest waitUntil(final Context context, final Void input, Persistence persistence, final Communication communication) {
        return CommandRequest.empty;
    }

    @Override
    public StateDecision execute(final Context context, final Void input, final CommandResults commandResults, Persistence persistence, final Communication communication) {
        return StateDecision.gracefulCompleteObjectExecution();
    }
}
