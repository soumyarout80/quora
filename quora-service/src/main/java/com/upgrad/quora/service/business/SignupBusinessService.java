package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupBusinessService {

    @Autowired
    private UserDao userDao;
    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signup(UserEntity userEntity)
            throws SignUpRestrictedException {

        //Procced only if the userName hasn't been used already
        if (userDao.getUserByUsername(userEntity.getUserName()) == null) {

            //Procced only if the email hasn't been used already
            if (userDao.getUserByEmail(userEntity.getEmail()) == null) {
                String password = userEntity.getPassword();
                String[] encryptedText = cryptographyProvider.encrypt(password);
                userEntity.setSalt(encryptedText[0]);
                userEntity.setPassword(encryptedText[1]);
                return userDao.createUser(userEntity);
            } else {
                throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
            }
        } else {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }
    }

    /* Written for checking validity of access token and role. Discarded later as the logic was combined into AuthorizationService

    public UserAuthTokenEntity getUserByAccessToken(String authorizationToken) throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        if (userAuthTokenEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete a question");
        }
        UserEntity userEntity =  userDao.getUserById(userAuthTokenEntity.getAuthUuid());
        if (userEntity.getRole().equalsIgnoreCase("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
        }
        return userAuthTokenEntity;
    }
*/
}

