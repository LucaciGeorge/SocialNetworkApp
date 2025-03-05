package repository.file;


import domain.Friendship;
import domain.validators.Validator;

import java.time.LocalDateTime;

public class FriendshipRepository extends AbstractFileRepository<Long, Friendship> {
    public FriendshipRepository(Validator<Friendship> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    public Friendship lineToEntity(String line) {
        String[] tokens = line.split(";");
        Long id = Long.parseLong(tokens[0]);
        Long firstUserId = Long.parseLong(tokens[1]);
        Long secondUserId = Long.parseLong(tokens[2]);

        LocalDateTime friendsFrom = tokens.length > 3 ? LocalDateTime.parse(tokens[3]) : null;

        Friendship prietenie = new Friendship(firstUserId, secondUserId, 1L, friendsFrom);
        prietenie.setId(id);

        return prietenie;
    }

    @Override
    public String entityToLine(Friendship entity) {
        return entity.getId() + ";" + entity.getFirstUserId() + ";" + entity.getSecondUserId();
    }
}

