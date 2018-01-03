package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import models.Actor;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import repository.ActorRepository;
import repository.FilmRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Manage a database of computers
 */
public class ActorController extends Controller {

    private final ActorRepository actorRepository;
    private final FilmRepository filmRepository;
    private final FormFactory formFactory;
    private final HttpExecutionContext httpExecutionContext;

    @Inject
    public ActorController(FormFactory formFactory,
                           ActorRepository actorRepository,
                           FilmRepository filmRepository,
                           HttpExecutionContext httpExecutionContext) {
        this.actorRepository = actorRepository;
        this.filmRepository = filmRepository;
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
    }

    /**
     * This result directly redirect to application home.
     */
    private Result GO_HOME = Results.redirect(
            routes.ActorController.list(0, "first_name", "asc", "")
    );

    /**
     * Handle default path requests, redirect to computers list
     */
    public Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of actors.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public CompletionStage<Result> list(int page, String sortBy, String order, String filter) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return actorRepository.page(page, 10, sortBy, order, filter).thenApplyAsync(list -> {
            // This is the HTTP rendering thread context
            return ok(views.html.list2.render(list, sortBy, order, filter));
        }, httpExecutionContext.current());
    }

    public CompletionStage<Result> json() {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return actorRepository.json().thenApplyAsync(list -> {
            // This is the HTTP rendering thread context
            JsonNode actorJson = Json.toJson(list);
            return ok(actorJson);
        }, httpExecutionContext.current());
    }
    /**
     * Display the paginated list of films.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public CompletionStage<Result> list3(int page, String sortBy, String order, String filter) {
        // Run a db operation in another thread (using DatabaseExecutionContext)
        return filmRepository.page(page, 10, sortBy, order, filter).thenApplyAsync(list -> {
            // This is the HTTP rendering thread context
            return ok(views.html.list3.render(list, sortBy, order, filter));
        }, httpExecutionContext.current());
    }


    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the actor to edit
     */
    public CompletionStage<Result> edit(Long id) {

        // Run a db operation in another thread (using DatabaseExecutionContext)
        CompletionStage<Map<String, String>> filmsFuture = filmRepository.options();

        // Run the lookup also in another thread, then combine the results:
        return actorRepository.lookup(id).thenCombineAsync(filmsFuture, (actorOptional, films) -> {
            // This is the HTTP rendering thread context
            Actor a = actorOptional.get();
            Form<Actor> actorForm = formFactory.form(Actor.class).fill(a);
            return ok(views.html.editForm2.render(id, actorForm, films));
        }, httpExecutionContext.current());
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the actor to edit
     */
    public CompletionStage<Result> update(Long id) throws PersistenceException {
        Form<Actor> actorForm = formFactory.form(Actor.class).bindFromRequest();
        if (actorForm.hasErrors()) {
            // Run companies db operation and then render the failure case
            return filmRepository.options().thenApplyAsync(films -> {
                // This is the HTTP rendering thread context
                return badRequest(views.html.editForm2.render(id, actorForm, films));
            }, httpExecutionContext.current());
        } else {
            Actor newActorData = actorForm.get();
            // Run update operation and then flash and then redirect
            return actorRepository.update(id, newActorData).thenApplyAsync(data -> {
                // This is the HTTP rendering thread context
                flash("success", "Actor " + newActorData.first_name + " has been updated");
                return GO_HOME;
            }, httpExecutionContext.current());
        }
    }

    /**
     * Display the 'new computer form'.
     */
//    public CompletionStage<Result> create() {
//        Form<Computer> computerForm = formFactory.form(Computer.class);
//        // Run companies db operation and then render the form
//        return companyRepository.options().thenApplyAsync((Map<String, String> companies) -> {
//            // This is the HTTP rendering thread context
//            return ok(views.html.createForm.render(computerForm, companies));
//        }, httpExecutionContext.current());
//    }

    /**
     * Handle the 'new computer form' submission
     */
//    public CompletionStage<Result> save() {
//        Form<Computer> computerForm = formFactory.form(Computer.class).bindFromRequest();
//        if (computerForm.hasErrors()) {
//            // Run companies db operation and then render the form
//            return companyRepository.options().thenApplyAsync(companies -> {
//                // This is the HTTP rendering thread context
//                return badRequest(views.html.createForm.render(computerForm, companies));
//            }, httpExecutionContext.current());
//        }
//
//        Computer computer = computerForm.get();
//        // Run insert db operation, then redirect
//        return computerRepository.insert(computer).thenApplyAsync(data -> {
//            // This is the HTTP rendering thread context
//            flash("success", "Computer " + computer.name + " has been created");
//            return GO_HOME;
//        }, httpExecutionContext.current());
//    }

    /**
     * Handle computer deletion
     */
    public CompletionStage<Result> delete(Long id) {
        // Run delete db operation, then redirect
        return actorRepository.delete(id).thenApplyAsync(v -> {
            // This is the HTTP rendering thread context
            flash("success", "Actor has been deleted");
            return GO_HOME;
        }, httpExecutionContext.current());
    }

}
            
