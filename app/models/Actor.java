package models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(mappedBy = "actors")
    @JsonManagedReference
    public List<Film> films = new ArrayList<Film>();
//    public Film film;

}

