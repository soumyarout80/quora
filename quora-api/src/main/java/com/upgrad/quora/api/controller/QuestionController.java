package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.business.UserProfileService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


// Controller class for question-operations.
@RestController
@RequestMapping("/")
public class QuestionController {


    // Autowired SignupBusiness service from quora business service
    @Autowired
    SignupBusinessService signupBusinessService;

    // Autowired question service from quora business service
    @Autowired
    QuestionService questionService;

    // Autowired authorization service from quora business service
    @Autowired
    private AuthorizationService authorizationService;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Rest Endpoint method implementation used for creating question for authorized user.
     * Only logged-in user is allowed to create a question.
     *
     * @param questionRequest request object of question instance
     * @param authorization   access token of user
     * @return ResponseEntity object with response details of question
     * @throws AuthorizationFailedException
     */

    @RequestMapping(method = RequestMethod.POST, path = "/question/create",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createQuestion(final QuestionRequest questionRequest,
                                            @RequestHeader final String authorization)
            throws AuthorizationFailedException {
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.CREATE_QUESTION);
        UserEntity user = userAuthTokenEntity.getUser();
        Question question = new Question();
        question.setUser(userAuthTokenEntity.getUser());
        question.setUuid(UUID.randomUUID().toString());
        question.setContent(questionRequest.getContent());
        final ZonedDateTime now = ZonedDateTime.now();
        question.setDate(now);
        Question createdQuestion = questionService.createQuestion(question);
        //sends the response after questio is created
        QuestionResponse questionResponse = new QuestionResponse().id(createdQuestion.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

    /**
     * Rest Endpoint method implementation used for getting all questions for any user.
     * Only logged-in user and the owner of the question is allowed to use this endpoint.
     *
     * @param authorization Authorized user
     * @return Response Entity of type QuestionEditResponse
     * @throws AuthorizationFailedException if user is not signed then this exception is thrown
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllQuestions(@RequestHeader final String authorization) throws
            AuthorizationFailedException {
        // authorization for the user or owner whether signed in
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.ALL_QUESTION);
        List<Question> questionList = questionService.getAllQuestions();
        StringBuilder builder = new StringBuilder();
        getContentsString(questionList, builder);
        StringBuilder uuIdBuilder = new StringBuilder();
        getUuIdString(questionList, uuIdBuilder);
        //fetches all the question
        QuestionDetailsResponse questionResponse = new QuestionDetailsResponse()
                .id(uuIdBuilder.toString())
                .content(builder.toString());
        return new ResponseEntity<QuestionDetailsResponse>(questionResponse, HttpStatus.OK);
    }

    /**
     * Rest Endpoint method implementation used for getting all questions for any user.
     * Only logged-in user and the owner of the question is allowed to use this endpoint.
     *
     * @param questionEditRequest request for question to be edited
     * @param questionId          question to be edited
     * @param authorization       Authorized user
     * @return Response Entity of type QuestionEditResponse
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException
     */

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editQuestionContent(QuestionEditRequest questionEditRequest,
                                                 @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws
            AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.EDIT_QUESTION);
        Question question = questionService.isUserQuestionOwner(questionId, userAuthTokenEntity, ActionType.EDIT_QUESTION);
        question.setContent(questionEditRequest.getContent());
        //edits the question
        questionService.editQuestion(question);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(question.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }

    //Rest Endpoint method implementation used for deleting question by question id.
    //Only logged-in user who is owner of the question or admin is allowed to delete a question
    /*
     * @param questionUuId  questionid to be deleted
     * @param userAuthTokenEntity to be authorized
     * @return ResponseEnitty object of type QuestionDeleteResponse
     * @throws AuthorizationFailedException and InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> userDelete(@PathVariable("questionId") final String questionUuId,
                                        @RequestHeader("authorization") final String authorization) throws
            AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.DELETE_QUESTION);
        Question question = questionService.isUserQuestionOwner(questionUuId, userAuthTokenEntity, ActionType.DELETE_QUESTION);
        // deletes the question
        questionService.deleteQuestion(question);
        QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse()
                .id(question.getUuid())
                .status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
    }

    //Rest Endpoint method implementation used for gets all question for the user id.
    //Only logged-in user who is owner of the question or admin is allowed to delete a question
    /*
     * @param userId  gets all the question for the userId
     * @param userAuthTokenEntity to be authorized
     * @return ResponseEnitty object of type QuestionDeleteResponse
     * @throws AuthorizationFailedException and UserNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllQuestionsByUser(@PathVariable("userId") final String uuId,
                                                   @RequestHeader("authorization") final String authorization) throws
            AuthorizationFailedException, UserNotFoundException {
        // user who has logged in
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.ALL_QUESTION_FOR_USER);
        List<Question> questionList = questionService.getQuestionsForUser(uuId);
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder uuIdBuilder = new StringBuilder();
        getContentsString(questionList, contentBuilder);
        getUuIdString(questionList, uuIdBuilder);
        //fetches all the questions for the user id
        QuestionDetailsResponse questionResponse = new QuestionDetailsResponse()
                .id(uuIdBuilder.toString())
                .content(contentBuilder.toString());
        return new ResponseEntity<QuestionDetailsResponse>(questionResponse, HttpStatus.OK);
    }

    /**
     * method for appending the uuid of questions.
     *
     * @param questionList List of questions
     * @param uuIdBuilder  StringBuilder object
     */

    public static final StringBuilder getUuIdString(List<Question> questionList, StringBuilder uuIdBuilder) {
        for (Question questionObject : questionList) {
            uuIdBuilder.append(questionObject.getUuid()).append(",");
        }
        return uuIdBuilder;
    }

    /**
     * method for providing contents string in appended format
     *
     * @param questionList list of questions
     * @param builder      StringBuilder with appended content list.
     */

    public static final StringBuilder getContentsString(List<Question> questionList, StringBuilder builder) {
        for (Question questionObject : questionList) {
            builder.append(questionObject.getContent()).append(",");
        }
        return builder;
    }

}



