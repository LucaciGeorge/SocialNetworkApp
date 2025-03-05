package repository;

import domain.Entity;
import domain.Friendship;
import utils.Pageable.Page;

import utils.Pageable.Pageable;



public interface FriendshipPageRepository<L extends Number, F extends Entity<Long>> extends PagingRepository<Long, Friendship> {
    Page<Friendship> findAllOnPage(Pageable pageable, Long id);

    Page<Friendship> findAllOnPageRequests(Pageable pageable, Long id);
}
