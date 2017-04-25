package org.jbpm.jsf.core.handler;

import org.jboss.gravel.common.annotation.TldAttribute;
import org.jboss.gravel.common.annotation.TldTag;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.action.StartProcessActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;

/**
 *
 */
@TldTag (
    name = "startProcess",
    description = "Start a process.",
    attributes = {
        @TldAttribute (
            name = "process",
            description = "The process definition to begin executing.",
            required = true,
            deferredType = ProcessDefinition.class
        ),
        @TldAttribute (
            name = "target",
            description = "An EL expression into which the new process instance will be stored.",
            deferredType = ProcessInstance.class
        )
    }
)
public final class StartProcessHandler extends AbstractHandler {
    private final TagAttribute processTagAttribute;
    private final TagAttribute instanceTagAttribute;

    public StartProcessHandler(final TagConfig config) {
        super(config);
        processTagAttribute = getRequiredAttribute("process");
        instanceTagAttribute = getRequiredAttribute("target");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new StartProcessActionListener(
            getValueExpression(processTagAttribute, ctx, ProcessDefinition.class),
            getValueExpression(instanceTagAttribute, ctx, ProcessInstance.class)
        );
    }
}
