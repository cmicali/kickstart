package com.cr.kickstart.data.model;

import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Author: chris
 */
@MappedSuperclass
public abstract class EntityBase implements Serializable {

    public enum JSONResponseType { SHORT, LONG }

    private Date dateCreated;
    private Date dateModified;
    private Boolean deleted;

    protected EntityBase() {
        deleted = false;
        touch();
    }

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateCreated() {
        return dateCreated;
    }

    protected void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateModified() {
        return dateModified;
    }

    protected void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    @Column(nullable = false, columnDefinition = "boolean default false")
    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void delete() {
        setDeleted(true);
    }

    protected void touch() {
        if (dateCreated == null) {
            setDateCreated(new Date());
        }
        setDateModified(new Date());
    }

    public JSONObject toJSON() throws JSONException {
        return toJSON(JSONResponseType.SHORT, null);
    }

    public JSONObject toJSON(EntityManager em) throws JSONException {
        return toJSON(JSONResponseType.SHORT, em);
    }

    public JSONObject toJSON(JSONResponseType type, EntityManager em) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("date_modified", getDateModified().getTime());
        return jo;
    }

}
