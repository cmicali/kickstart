package com.cr.kickstart.data.model;

import org.hibernate.search.annotations.DocumentId;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Author: chris
 */
@MappedSuperclass
public abstract class EntityWithIDBase extends EntityBase implements Serializable {

    private Long id;

    protected EntityWithIDBase() {
        super();
    }

    @Id
    @DocumentId
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public JSONObject toJSON(JSONResponseType type, EntityManager em) throws JSONException {
        JSONObject jo = super.toJSON(type, em);
        jo.put("id", getId());
        return jo;
    }

}
