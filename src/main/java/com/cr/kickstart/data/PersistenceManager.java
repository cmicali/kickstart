package com.cr.kickstart.data;

import com.cr.kickstart.framework.ServerConfig;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;

/**
 * User: chris
 */
public class PersistenceManager {

    public static final String PERSISTENCE_UNIT = "app.data";

    private static final PersistenceManager INSTANCE = new PersistenceManager();

    protected EntityManagerFactory emf;

    private PersistenceManager() {

    }

    public static PersistenceManager getInstance() {
        return INSTANCE;
    }

    public EntityManager getEntityManager(ServletContext servletContext) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
//        ReferenceDataUtil.initializeReferenceData(em);
//        TestDataInsertUtil.initializeTestData(servletContext, em);
        return em;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT) ;
        }
        return emf;
    }

    public void closeEntityManagerFactory() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }
}
