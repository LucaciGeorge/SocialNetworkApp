package repository.file;

import domain.User;
import domain.validators.Validator;

public class UserRepository extends AbstractFileRepository<Long, User>{
    public UserRepository(Validator<User> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    public User lineToEntity(String line) {
        String[] splited = line.split(";");
        User u = new User(splited[1], splited[2], splited[3], splited[4], null, null);
        u.setId(Long.parseLong(splited[0]));
        return u;
    }

    @Override
    public String entityToLine(User entity) {
        return entity.getId() + ";" + entity.getFirstName() + ";" + entity.getLastName();
    }
}
