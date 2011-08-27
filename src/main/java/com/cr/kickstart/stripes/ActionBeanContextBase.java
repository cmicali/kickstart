package com.cr.kickstart.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;

/**
 * Author: chris
 */
public class ActionBeanContextBase extends ActionBeanContext {

    /** Gets the currently logged in user, or null if no-one is logged in. */
    /*
    public User getUser() {
        return (User) getRequest().getSession().getAttribute("user");
    }
    */

    /** Sets the currently logged in user. */
    /*
    public void setUser(User currentUser) {
        getRequest().getSession().setAttribute("user", currentUser);
    }

    public boolean isUserLoggedIn() {
        return getUser() != null;
    }
    */

    /** Logs the user out by invalidating the session. */
    public void logout() {
        getRequest().getSession().invalidate();
    }
    
}
