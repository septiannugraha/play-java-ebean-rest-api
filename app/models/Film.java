package models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.List;


/**
 * Computer entity managed by Ebean
 */
@Entity 
public class Film extends BaseModel {

    private static final long serialVersionUID = 1L;

    @Constraints.Required
    public String title;

    @Constraints.Required
    public String description;

    @ManyToMany
    @JoinTable(name="film_actor",
            joinColumns=@JoinColumn(name="film_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="actor_id", referencedColumnName="id"))
    @JsonBackReference
    public List<Actor> actors;
//    public Actor actor;

}

