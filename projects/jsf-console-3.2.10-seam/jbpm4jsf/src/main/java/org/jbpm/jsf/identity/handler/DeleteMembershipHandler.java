package org.jbpm.jsf.identity.handler;

import org.jboss.gravel.common.annotation.TldAttribute;
import org.jboss.gravel.common.annotation.TldTag;
import org.jbpm.identity.User;
import org.jbpm.jsf.JbpmActionListener;
import org.jbpm.jsf.core.handler.AbstractHandler;
import org.jbpm.jsf.identity.action.DeleteMembershipActionListener;

import com.sun.facelets.FaceletContext;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagConfig;

/**
 *
 */
@TldTag (
    name = "deleteMembership",
    description = "Delete a membership from the database.",
    attributes = {
        @TldAttribute (
            name = "user",
            description = "The user containing the membership to delete.",
            required = true,
            deferredType = User.class
        ),
        @TldAttribute (
            name = "membershipId",
            description = "The ID of the membership to delete.",
            required = true,
            deferredType = long.class
        )
    }
)
public final class DeleteMembershipHandler extends AbstractHandler {
    private final TagAttribute userAttribute;
    private final TagAttribute membershipIdAttribute;

    public DeleteMembershipHandler(final TagConfig config) {
        super(config);
        userAttribute = getRequiredAttribute("user");
        membershipIdAttribute = getRequiredAttribute("membershipId");
    }

    protected JbpmActionListener getListener(final FaceletContext ctx) {
        return new DeleteMembershipActionListener(
            getValueExpression(userAttribute, ctx, User.class),
            getValueExpression(membershipIdAttribute, ctx, Long.class)
        );
    }
}
