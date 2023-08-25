package io.iworkflow.integ.stateapifail;

import io.iworkflow.core.ObjectWorkflow;
import io.iworkflow.core.StateDef;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ExecuteApiFailProceedWorkflow implements ObjectWorkflow {
    @Override
    public List<StateDef> getWorkflowStates() {
        return Arrays.asList(
                StateDef.startingState(new StateApiFailProceedState()),
                StateDef.nonStartingState(new StateApiRecoverState())
        );
    }
}
