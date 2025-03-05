package domain.validators;


import domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ServiceException {
        if(entity.getFirstName().equals("") || entity.getLastName().equals(""))
            throw new ValidationException("Utilizatorul nu este valid");
    }
}

