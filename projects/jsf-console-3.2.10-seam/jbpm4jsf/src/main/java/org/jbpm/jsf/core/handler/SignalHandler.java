package org.jbpm.jsf.core.handler;

import org.jboss.gravel.common.annotation.TldAttribute;
import org.jboss.gravel.common.annotation.TldTag;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.action.SignalActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;

@TldTag (
    name = "signal",
    description = "Signal a token or process instance.  Optionally specify the transition.",
    attributes = {
        @TldAttribute (
            name = "value",
            description = "The item to signal.",
            required = true,
            deferredType = Object.class
        ),
        @TldAttribute (
            name = "transition",
            description = "The transition, or transition name, to signal the item along.",
            deferredType = Object.class
        )
    }
)
public final class SignalHandler extends AbstractHandler {
    private final TagAttribute valueTagAttribute;
    private final TagAttribute transitionTagAttribute;

    public SignalHandler(final TagConfig config) {
        super(config);
        valueTagAttribute = getRequiredAttribute("value");
        transitionTagAttribute = getAttribute("transition");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new SignalActionListener(
            getValueExpression(valueTagAttribute, ctx, Object.class),
            getValueExpression(transitionTagAttribute, ctx, Object.class)
        );
    }
}
