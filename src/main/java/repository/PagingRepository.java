package repository;

import domain.Entity;
import repository.Repository;
import utils.Pageable.Page;
import utils.Pageable.Pageable;

public interface PagingRepository<ID, E extends Entity<ID>> extends Repository<ID,E> {
    Page<E> findAllOnPage(Pageable pageable, ID id);
}
