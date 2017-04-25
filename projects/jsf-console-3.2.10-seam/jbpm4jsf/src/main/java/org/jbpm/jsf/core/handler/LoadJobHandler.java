package org.jbpm.jsf.core.handler;

import org.jboss.gravel.common.annotation.TldAttribute;
import org.jboss.gravel.common.annotation.TldTag;
import org.jbpm.job.Job;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.action.LoadJobActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;

/**
 *
 */
@TldTag (
    name = "loadJob",
    description = "Read a job instance from the database.",
    attributes = {
        @TldAttribute (
            name = "id",
            description = "The ID of the job to load.",
            required = true,
            deferredType = long.class
        ),
        @TldAttribute (
            name = "target",
            description = "An EL expression into which the job should be stored.",
            required = true,
            deferredType = Job.class
        )
    }
)
public final class LoadJobHandler extends AbstractHandler {
    private final TagAttribute idTagAttribute;
    private final TagAttribute targetTagAttribute;

    public LoadJobHandler(final TagConfig config) {
        super(config);
        idTagAttribute = getRequiredAttribute("id");
        targetTagAttribute = getRequiredAttribute("target");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new LoadJobActionListener(
            getValueExpression(idTagAttribute, ctx, Long.class),
            getValueExpression(targetTagAttribute, ctx, Job.class)
        );
    }
}
