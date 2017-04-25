package org.jbpm.jsf.identity.handler;

import org.jboss.gravel.common.annotation.TldAttribute;
import org.jboss.gravel.common.annotation.TldTag;
import org.jbpm.identity.User;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.handler.AbstractHandler;
import org.jbpm.jsf.identity.action.DeleteUserActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;

/**
 *
 */
@TldTag (
    name = "deleteUser",
    description = "Delete a user from the database.",
    attributes = {
        @TldAttribute (
            name = "user",
            description = "The user to delete.",
            required = true,
            deferredType = User.class
        )
    }
)
public final class DeleteUserHandler extends AbstractHandler {
    private final TagAttribute userAttribute;

    public DeleteUserHandler(final TagConfig config) {
        super(config);
        userAttribute = getRequiredAttribute("user");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new DeleteUserActionListener(
            getValueExpression(userAttribute, ctx, User.class)
        );
    }
}
