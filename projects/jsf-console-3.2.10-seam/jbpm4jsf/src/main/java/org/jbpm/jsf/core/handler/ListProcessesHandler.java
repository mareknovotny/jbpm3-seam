package org.jbpm.jsf.core.handler;

import org.jboss.gravel.common.annotation.TldTag;
import org.jboss.gravel.common.annotation.TldAttribute;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.action.ListProcessesActionListener;

import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;
import com.sun.facelets.FaceletContext;

import java.util.List;

/**
 *
 */
@TldTag (
    name = "listProcesses",
    description = "Read a list of process definitions from the database.",
    attributes = {
        @TldAttribute (
            name = "target",
            description = "An EL expression into which the process definition list should be stored.",
            required = true,
            deferredType = List.class
        )
    }
)
public final class ListProcessesHandler extends AbstractHandler {
    private final TagAttribute targetTagAttribute;

    public ListProcessesHandler(final TagConfig config) {
        super(config);
        targetTagAttribute = getRequiredAttribute("target");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new ListProcessesActionListener(
            getValueExpression(targetTagAttribute, ctx, List.class)
        );
    }
}
