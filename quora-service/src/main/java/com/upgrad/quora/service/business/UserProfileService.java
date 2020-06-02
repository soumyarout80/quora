package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthorizationService authorizationService;

    //Service to fetch user details matching UUID
    //Gets String UUID and String authorization as input
    public UserEntity fetchUser(final String uuid, final String authorization)
            throws AuthorizationFailedException, UserNotFoundException {
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.GET_USER_DETAILS);
        final UserEntity fetchedUser = userDao.getUserById(uuid);
        return fetchedUser;
    }
}