package dataaccess;

import model.AuthData;
import model.UserData;

public interface DataAccess {
    abstract class AuthDAO {
        public abstract AuthData createAuth(UserData userData);
    }
    abstract class UserDAO {
        public abstract UserData getUser(UserData userData);
        public abstract void createUser(UserData userData);
    }
}
