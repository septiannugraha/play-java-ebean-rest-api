package repository;

import io.ebean.*;
import models.Actor;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class ActorRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public ActorRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of actor
     *
     * @param page     Page to display
     * @param pageSize Number of actors per page
     * @param sortBy   Computer property used for sorting
     * @param order    Sort order (either or asc or desc)
     * @param filter   Filter applied on the name column
     */
    public CompletionStage<PagedList<Actor>> page(int page, int pageSize, String sortBy, String order, String filter) {
        return supplyAsync(() ->
                ebeanServer.find(Actor.class).where()
                    .ilike("first_name", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<List<Actor>> json() {
        return supplyAsync(() ->
                ebeanServer.find(Actor.class).findList(), executionContext);
    }

    public CompletionStage<Optional<Actor>> lookup(Long id) {
        return supplyAsync(() -> Optional.ofNullable(ebeanServer.find(Actor.class).setId(id).findOne()), executionContext);
    }

    public CompletionStage<Optional<Long>> update(Long id, Actor newActorData) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                Actor savedActor = ebeanServer.find(Actor.class).setId(id).findOne();
                if (savedActor!= null) {
                    savedActor.first_name = newActorData.first_name;
                    savedActor.last_name = newActorData.last_name;

                    savedActor.update();
                    txn.commit();
                    value = Optional.of(id);
                }
            } finally {
                txn.end();
            }
            return value;
        }, executionContext);
    }

    public CompletionStage<Optional<Long>>  delete(Long id) {
        return supplyAsync(() -> {
            try {
                final Optional<Actor> actorOptional = Optional.ofNullable(ebeanServer.find(Actor.class).setId(id).findOne());
                actorOptional.ifPresent(Model::delete);
                return actorOptional.map(c -> c.id);
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    public CompletionStage<Long> insert(Actor actor) {
        return supplyAsync(() -> {
             actor.id = System.currentTimeMillis(); // not ideal, but it works
             ebeanServer.insert(actor);
             return actor.id;
        }, executionContext);
    }

}
