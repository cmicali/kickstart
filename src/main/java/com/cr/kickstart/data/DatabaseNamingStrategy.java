package com.cr.kickstart.data;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Custom naming strategy for the Hibernate DB column name generator.  Makes all the column names
 * in the DB LookLikeThis insteadOfLikeThis and adds ID to end of FK columns.
 *
 * Author: chris
 */
public class DatabaseNamingStrategy extends ImprovedNamingStrategy {

    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        String s = super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName);
        s = s.endsWith("_id") ? s : s + "_id";
        return s;
    }

}