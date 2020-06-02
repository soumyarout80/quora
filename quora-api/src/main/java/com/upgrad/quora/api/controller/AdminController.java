package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.SignoutService;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/*This class implements the userDelete - "/admin/user/{userId}"*/
@RestController
@RequestMapping("/")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthorizationService authorizationService;

    /*This endpoint is used to delete a user from the Quora application if the user has signed in and has valid user access token
    and has admin role. If any of these conditions fail, the corresponding exception is thrown.
    This endpoint (a DELETE request), requests path variable userId as a string for the corresponding user that needs
    to be deleted and access token of the signed in user as a String in authorization Request Header. It returns the uuid
    of the user that has been deleted and message in the JSON response with the corresponding HTTP status*/
    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> deleteUser(@PathVariable("userId") final String uuid, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, UserNotFoundException {

        String UUID = adminService.deleteUser(uuid, authorization);
        /*UserDeletResposnse will have the message that user has been succeefully deleted*/
        final UserDeleteResponse userDeleteResponse = new UserDeleteResponse().id(UUID).status("USER SUCCESSFULLY DELETED");

        /*Returning the message in the JSON response with the corresponding HTTP status*/
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);

    }
}
