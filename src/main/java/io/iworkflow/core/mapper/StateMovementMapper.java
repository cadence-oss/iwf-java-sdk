package io.iworkflow.core.mapper;

import io.iworkflow.core.ObjectEncoder;
import io.iworkflow.core.Registry;
import io.iworkflow.core.StateDef;
import io.iworkflow.core.WorkflowStateOptions;
import io.iworkflow.core.WorkflowDefinitionException;
import io.iworkflow.core.WorkflowState;
import io.iworkflow.gen.models.ExecuteApiFailurePolicy;
import io.iworkflow.gen.models.RetryPolicy;
import io.iworkflow.gen.models.StateMovement;
import io.iworkflow.gen.models.WaitUntilApiFailurePolicy;

import static io.iworkflow.core.StateMovement.RESERVED_STATE_ID_PREFIX;
import static io.iworkflow.core.WorkflowState.shouldSkipWaitUntil;
import static io.iworkflow.core.WorkflowStateOptionsExtension.deepCopyStateOptions;

public class StateMovementMapper {

    public static StateMovement toGenerated(final io.iworkflow.core.StateMovement stateMovement, final String workflowType, final Registry registry, final ObjectEncoder objectEncoder) {
        final Object input = stateMovement.getStateInput().orElse(null);
        final StateMovement movement = new StateMovement()
                .stateId(stateMovement.getStateId())
                .stateInput(objectEncoder.encode(input));
        if (!stateMovement.getStateId().startsWith(RESERVED_STATE_ID_PREFIX)) {
            final StateDef stateDef = registry.getWorkflowState(workflowType, stateMovement.getStateId());
            if(stateDef == null){
                throw new IllegalArgumentException("state "+stateMovement.getStateId() +" is not registered in the workflow "+workflowType);
            }

            // Try to get the overrode stateOptions, if it's null, get the stateOptions from stateDef
            io.iworkflow.gen.models.WorkflowStateOptions stateOptions;
            if (stateMovement.getStateOptionsOverride().isPresent()) {
                // Always deep copy the state options so we don't modify the original
                stateOptions = toIdlWorkflowStateOptions(deepCopyStateOptions(stateMovement.getStateOptionsOverride().get()));
            } else {
                stateOptions = StateMovementMapper.validateAndGetIdlStateOptions(stateDef);
            }

            if (shouldSkipWaitUntil(stateDef.getWorkflowState())) {
                if (stateOptions == null) {
                    stateOptions = new io.iworkflow.gen.models.WorkflowStateOptions();
                }

                stateOptions.skipWaitUntil(true);
            }

            if (stateOptions != null) {
                movement.stateOptions(stateOptions);
            }

            stateMovement.getWaitForKey().ifPresent(movement::waitForKey);
        }
        return movement;
    }

    public static io.iworkflow.gen.models.WorkflowStateOptions validateAndGetIdlStateOptions(final StateDef stateDef){
        final WorkflowState state = stateDef.getWorkflowState();
        // Always deep copy the state options so we don't modify the original
        WorkflowStateOptions stateOptions = deepCopyStateOptions(state.getStateOptions());
        if (stateOptions == null){
            return null;
        }
        if(stateOptions.getExecuteApiFailurePolicy() == ExecuteApiFailurePolicy.PROCEED_TO_CONFIGURED_STATE){
            // retry policy must be set
            if(stateOptions.getExecuteApiRetryPolicy() == null){
                throw new WorkflowDefinitionException("RetryPolicy must be set for the execute "+state.getStateId());
            }
            final RetryPolicy policy = stateOptions.getExecuteApiRetryPolicy();
            // either maximumAttempts or maximumAttemptsDurationSeconds must be set and greater than zero
            if(policy.getMaximumAttempts() == null && policy.getMaximumAttemptsDurationSeconds() == null){
                throw new WorkflowDefinitionException("Either maximumAttempts or maximumAttemptsDurationSeconds must be set for the execute "+state.getStateId());
            }
        }
        if(stateOptions.getWaitUntilApiFailurePolicy() == WaitUntilApiFailurePolicy.FAIL_WORKFLOW_ON_FAILURE){
            // retry policy must be set
            if(stateOptions.getWaitUntilApiRetryPolicy() == null){
                throw new WorkflowDefinitionException("RetryPolicy must be set for the waitUntil "+state.getStateId());
            }
            final RetryPolicy policy = stateOptions.getWaitUntilApiRetryPolicy();
            // either maximumAttempts or maximumAttemptsDurationSeconds must be set and greater than zero
            if(policy.getMaximumAttempts() == null && policy.getMaximumAttemptsDurationSeconds() == null){
                throw new WorkflowDefinitionException("Either maximumAttempts or maximumAttemptsDurationSeconds must be set for the waitUntil "+state.getStateId());
            }
        }
        return toIdlWorkflowStateOptions(stateOptions);
    }

    public static io.iworkflow.gen.models.WorkflowStateOptions toIdlWorkflowStateOptions(WorkflowStateOptions workflowStateOptions) {
        if (workflowStateOptions == null) {
            return null;
        }

        final io.iworkflow.gen.models.WorkflowStateOptions idlWorkflowStateOptions =
                new io.iworkflow.gen.models.WorkflowStateOptions();

        idlWorkflowStateOptions.setSearchAttributesLoadingPolicy(workflowStateOptions.getSearchAttributesLoadingPolicy());
        idlWorkflowStateOptions.setWaitUntilApiSearchAttributesLoadingPolicy(workflowStateOptions.getWaitUntilApiSearchAttributesLoadingPolicy());
        idlWorkflowStateOptions.setExecuteApiSearchAttributesLoadingPolicy(workflowStateOptions.getExecuteApiSearchAttributesLoadingPolicy());
        idlWorkflowStateOptions.setDataAttributesLoadingPolicy(workflowStateOptions.getDataAttributesLoadingPolicy());
        idlWorkflowStateOptions.setWaitUntilApiDataAttributesLoadingPolicy(workflowStateOptions.getWaitUntilApiDataAttributesLoadingPolicy());
        idlWorkflowStateOptions.setExecuteApiDataAttributesLoadingPolicy(workflowStateOptions.getExecuteApiDataAttributesLoadingPolicy());
        idlWorkflowStateOptions.setWaitUntilApiTimeoutSeconds(workflowStateOptions.getWaitUntilApiTimeoutSeconds());
        idlWorkflowStateOptions.setExecuteApiTimeoutSeconds(workflowStateOptions.getExecuteApiTimeoutSeconds());
        idlWorkflowStateOptions.setWaitUntilApiRetryPolicy(workflowStateOptions.getWaitUntilApiRetryPolicy());
        idlWorkflowStateOptions.setExecuteApiRetryPolicy(workflowStateOptions.getExecuteApiRetryPolicy());
        idlWorkflowStateOptions.setWaitUntilApiFailurePolicy(workflowStateOptions.getWaitUntilApiFailurePolicy());
        idlWorkflowStateOptions.setExecuteApiFailurePolicy(workflowStateOptions.getExecuteApiFailurePolicy());
        idlWorkflowStateOptions.setExecuteApiFailureProceedStateOptions(toIdlWorkflowStateOptions(workflowStateOptions.getExecuteApiFailureProceedStateOptions()));

        return idlWorkflowStateOptions;
    }
}
