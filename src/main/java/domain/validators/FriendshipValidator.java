package domain.validators;

import domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship entity) throws ServiceException {
        if (entity.getFirstUserId() == null || entity.getSecondUserId() == null) {
            throw new ValidationException("Id-ul nu poate fi null");
        }
        if (entity.getFirstUserId().equals(entity.getSecondUserId())) {
            throw new ValidationException("Id-urile trebuie sa fie diferite");
        }
    }
}
