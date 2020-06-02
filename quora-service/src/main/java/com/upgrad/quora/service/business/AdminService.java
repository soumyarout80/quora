package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AdminDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private AdminDao adminDao;

    @Autowired
    private AuthorizationService authorizationService;

    /*This method propogates the transaction of deleting the user in the database if the signed in user is an admin
     and the user that has to be deleted has valid accesstoken*/

    /*The annotation @Transactional esatblishs the connection, performs the operation and commits the transaction*/
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteUser(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        /*Incorporated changes as per the changed User Dao method isValidAuthTokenForAdmin() */
        UserAuthTokenEntity authTokenEntity = userDao.isValidActiveAuthToken(authorization, ActionType.DELETE_USER);
        /*checking if user has signed in*/
        if (userDao.hasUserSignedIn(authorization)) {
            /*Checking if the user has signed out and accordingly throwing an exception*/
            if (authTokenEntity != null) {
                /*Check if the logged in user has admin role or not*/
                if (userDao.isRoleAdmin(authorization)) {
                    UserEntity userEntity = userDao.getUserById(userUuid);
                    /*Check if the user with the entered Uuid exists or not*/
                    if (userEntity == null) {
                        /*if user with the entered uuid does not exist throw an exception*/
                        throw new UserNotFoundException("USR-001", "User with the entered Uuid to be deleted does not exist");
                    } else {
                        /*if the user with the entered uuid exists, and the logged in user has admin role, we delete the given user*/
                        return adminDao.deleteUser(userUuid);
                    }
                } else {
                    throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
                }
            } else {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out");
            }
        } else {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
    }
}
