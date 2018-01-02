package models;

import play.data.validation.Constraints;

//import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

/**
 * Computer entity managed by Ebean
 */
@Entity 
public class Actor extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String first_name;

    @Constraints.Required
    public String last_name;

//    @ManyToMany
//    public Film film;

}

