package com.cr.kickstart.data;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * Created by IntelliJ IDEA.
 * User: cmicali
 * Date: 5/22/11
 * Time: 5:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBUtil {

    public static final String HINT_CACHEABLE = "org.hibernate.cacheable";

    public static Query setCacheable(Query q) {
        q.setHint(HINT_CACHEABLE, true);
        return q;
    }

    public static Query createCacheableQuery(EntityManager em, String query) {
        Query q = em.createQuery(query);
        return setCacheable(q);
    }

}
