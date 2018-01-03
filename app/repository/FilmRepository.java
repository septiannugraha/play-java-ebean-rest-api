package repository;

import io.ebean.*;
import models.Film;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * A repository that executes database operations in a different
 * execution context.
 */
public class FilmRepository {

    private final EbeanServer ebeanServer;
    private final DatabaseExecutionContext executionContext;

    @Inject
    public FilmRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    /**
     * Return a paged list of films
     *
     * @param page     Page to display
     * @param pageSize Number of actors per page
     * @param sortBy   Computer property used for sorting
     * @param order    Sort order (either or asc or desc)
     * @param filter   Filter applied on the name column
     */
    public CompletionStage<PagedList<Film>> page(int page, int pageSize, String sortBy, String order, String filter) {
        return supplyAsync(() ->
                ebeanServer.find(Film.class).where()
                    .ilike("title", "%" + filter + "%")
                    .orderBy(sortBy + " " + order)
                    .setFirstRow(page * pageSize)
                    .setMaxRows(pageSize)
                    .findPagedList(), executionContext);
    }

    public CompletionStage<Optional<Film>> lookup(Long id) {
        return supplyAsync(() -> Optional.ofNullable(ebeanServer.find(Film.class).setId(id).findOne()), executionContext);
    }

    public CompletionStage<Map<String, String>> options() {
        return supplyAsync(() -> ebeanServer.find(Film.class).orderBy("title").findList(), executionContext)
                .thenApply(list -> {
                    HashMap<String, String> options = new LinkedHashMap<String, String>();
                    for (Film c : list) {
                        options.put(c.id.toString(), c.title);
                    }
                    return options;
                });
    }

    public CompletionStage<Optional<Long>> update(Long id, Film newFilmData) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            Optional<Long> value = Optional.empty();
            try {
                Film savedFilm = ebeanServer.find(Film.class).setId(id).findOne();
                if (savedFilm!= null) {
//                    savedComputer.company = newComputerData.company;
//                    savedComputer.discontinued = newComputerData.discontinued;
//                    savedComputer.introduced = newComputerData.introduced;
                    savedFilm.title= newFilmData.title;
                    savedFilm.description= newFilmData.description;

                    savedFilm.update();
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
                final Optional<Film> filmOptional = Optional.ofNullable(ebeanServer.find(Film.class).setId(id).findOne());
                filmOptional.ifPresent(Model::delete);
                return filmOptional.map(c -> c.id);
            } catch (Exception e) {
                return Optional.empty();
            }
        }, executionContext);
    }

    public CompletionStage<Long> insert(Film film) {
        return supplyAsync(() -> {
             film.id = System.currentTimeMillis(); // not ideal, but it works
             ebeanServer.insert(film);
             return film.id;
        }, executionContext);
    }
}
