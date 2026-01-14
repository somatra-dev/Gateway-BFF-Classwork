package auth.res_server.demo.service;



import auth.res_server.demo.dto.user.CreateUser;
import auth.res_server.demo.dto.user.UpdateUser;
import auth.res_server.demo.dto.user.UserResponse;

import java.util.List;

public interface UserService {

    List<UserResponse> getAll();

    UserResponse getById(String id);

    void createUser(CreateUser create);

    void updateUserById(UpdateUser updateUser, String id);

    void deleteUserById(String id);
}
