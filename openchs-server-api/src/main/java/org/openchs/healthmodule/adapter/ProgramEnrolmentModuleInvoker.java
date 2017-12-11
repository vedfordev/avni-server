package org.openchs.healthmodule.adapter;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.openchs.dao.ConceptRepository;
import org.openchs.domain.Concept;
import org.openchs.healthmodule.adapter.contract.ChecklistRuleResponse;
import org.openchs.healthmodule.adapter.contract.DecisionRuleResponse;
import org.openchs.healthmodule.adapter.contract.ProgramEnrolmentDecisionRuleResponse;
import org.openchs.healthmodule.adapter.contract.ProgramEnrolmentRuleInput;
import org.openchs.web.request.ObservationRequest;

import javax.script.ScriptEngine;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgramEnrolmentModuleInvoker extends HealthModuleInvoker {
    public ProgramEnrolmentModuleInvoker(ScriptEngine scriptEngine, InputStream inputStream) {
        super(scriptEngine, inputStream);
    }

    public ChecklistRuleResponse getChecklist(ProgramEnrolmentRuleInput programEnrolmentRuleInput) {
        ScriptObjectMirror checklists = (ScriptObjectMirror) this.invoke("getChecklists", programEnrolmentRuleInput);
        if (checklists.containsKey("0"))
            return new ChecklistRuleResponse((ScriptObjectMirror) checklists.get("0"));
        else
            return null;
    }

    public List<ObservationRequest> getDecisions(ProgramEnrolmentRuleInput programEnrolmentRuleInput, ConceptRepository conceptRepository) {
        ScriptObjectMirror decision = (ScriptObjectMirror) this.invoke("getDecisions", programEnrolmentRuleInput);
        ProgramEnrolmentDecisionRuleResponse programEnrolmentDecisionRuleResponse = new ProgramEnrolmentDecisionRuleResponse(decision);
        List<DecisionRuleResponse> decisionRuleResponses = programEnrolmentDecisionRuleResponse.getDecisionRuleResponses();

        return getObservationRequests(conceptRepository, decisionRuleResponses);
    }
}