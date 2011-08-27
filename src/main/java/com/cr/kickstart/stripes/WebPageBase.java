package com.cr.kickstart.stripes;

import com.cr.kickstart.data.PersistenceManager;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

/**
 * Author: chris
 */
public abstract class WebPageBase implements ActionBean {

    private Logger log = LoggerFactory.getLogger(WebPageBase.class);

    private ActionBeanContextBase context;
    private EntityManager em = null;

    public void setContext(ActionBeanContext context) {
        this.context = (ActionBeanContextBase) context;
    }

    /** Gets the ActionBeanContext set by Stripes during initialization. */
    public ActionBeanContextBase getContext() {
        return this.context;
    }
/*
    public User getUser() {
        return getContext().getUser();
    }

    public boolean isUserLoggedIn() {
        return getContext().isUserLoggedIn();
    }
*/
    @Before()
    protected void onRequestStart() {

    }

    @After(stages = LifecycleStage.RequestComplete)
    protected void onRequestComplete() {
        if (em != null) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            if (em.isOpen()) {
                em.close();
            }
            em = null;
        }
    }

    protected EntityManager getEntityManager() {
        if (em == null) {
            em = PersistenceManager.getInstance().getEntityManager(getContext().getServletContext());
            em.getTransaction().begin();
        }
        return em;
    }

    protected ForwardResolution getNotFoundResolution() {
        return new ForwardResolution("/WEB-INF/jsp/notfound.jsp");
    }

}
