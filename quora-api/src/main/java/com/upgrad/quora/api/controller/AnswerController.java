package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerService;
import com.upgrad.quora.service.business.AuthorizationService;
import com.upgrad.quora.service.business.QuestionService;
import com.upgrad.quora.service.business.SignupBusinessService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.type.ActionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

// Controller for posting Answer and Edit,delete Answer
@RestController
@RequestMapping("/")
public class AnswerController {

    // Autowired answer service from quora business service
    @Autowired
    AnswerService answerService;

    // Autowired authorization service from quora business service
    @Autowired
    private AuthorizationService authorizationService;

    // Autowired question service from quora business service
    @Autowired
    QuestionService questionService;

    // This controller method is called when the request pattern is of
    // type 'createAnswer' and incoming request is of POST Type
    // The method calls the createAnswer() method in the business logic
    // Seeks for a controller method with mapping of type '/question/{questionId}answer/create'

    /**
     * Method is used to create answer with respect to question id
     *
     * @param answerRequest
     * @param questionUuId
     * @param authorization
     * @return answer response with the status created
     * @throws AuthorizationFailedException and throws InvalidQuestionException
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> createAnswer(final AnswerRequest answerRequest, @PathVariable("questionId") final String questionUuId, @RequestHeader final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.CREATE_ANSWER);
        //Gets the question object from the database
        Question question = questionService.getQuestionForUuId(questionUuId);
        //Gets the answer object from the database
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswer(answerRequest.getAnswer());
        answer.setUuid(UUID.randomUUID().toString());
        answer.setUser(userAuthTokenEntity.getUser());
        ZonedDateTime now = ZonedDateTime.now();
        answer.setDate(now);
        //sends the answer object created in the database
        Answer createdAnswer = answerService.createAnswer(answer);
        //Response object for the answer created
        AnswerResponse answerResponse = new AnswerResponse().id(createdAnswer.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    // This controller method is called when the request pattern is of
    // type 'editAnswerContent' and also the incoming request is of PUT Type
    // The method calls the editAnswer() method in the business logic service passing the answer to be updated
    // Seeks for a controller method with mapping of type '/answer/edit/{answerId}'

    /**
     * Method used edit answer with respect to answer id
     *
     * @param answerEditRequest
     * @param answerUuId
     * @param authorization
     * @return edited response with status ok
     * @throws AuthorizationFailedException and AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> editAnswerContent(AnswerEditRequest answerEditRequest, @PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {
        //Authorize the user
        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.EDIT_ANSWER);
        //Who answer for the question whether the owner or not
        Answer answer = answerService.isUserAnswerOwner(answerUuId, userAuthTokenEntity, ActionType.EDIT_ANSWER);
        // get the details that needs to be updated
        answer.setAnswer(answerEditRequest.getContent());
        answer.setDate(ZonedDateTime.now());
        // object of editanswer is created
        Answer editedAnswer = answerService.editAnswer(answer);
        // Respone to be sent for editedanswer updated in the database with the HttpStatus
        AnswerEditResponse answerEditResponse = new AnswerEditResponse()
                .id(answerUuId)
                .status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    // This controller method is called when the request pattern is of
    // type 'deleteAnswer' and incoming request is of DELETE Type
    // The method calls the deleteAnswer() method in the business logic service passing the answer to be deleted
    // seeks for a controller method with mapping of type '/answer/delete/{answerId}'

    /**
     * Method is used to delete particular answer for an question with respect to answer id
     *
     * @param answerUuId
     * @param authorization
     * @return With the status ok
     * @throws AuthorizationFailedException and  AnswerNotFoundException
     */

    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> deleteAnswer(@PathVariable("answerId") final String answerUuId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AnswerNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.DELETE_ANSWER);
        Answer answer = answerService.isUserAnswerOwner(answerUuId, userAuthTokenEntity, ActionType.DELETE_ANSWER);
        // deletes the answer from the answer business logic service
        answerService.deleteAnswer(answer);
        // sends the response whether the answer is deleted along with HttpStatus
        AnswerDeleteResponse answerDeleteResponse = new AnswerDeleteResponse()
                .id(answerUuId)
                .status("ANSWER DELETED");
        return new ResponseEntity<AnswerDeleteResponse>(answerDeleteResponse, HttpStatus.OK);
    }


    // This controller method is called when the request pattern is of
    // type // 'getAllAnswersToQuestion' and incoming request is of GET Type
    // seekss for a controller method with mapping of type '/answer/all/{questionId}'

    /**
     * Method is used to get all answer to particular question with respect to question id
     *
     * @param questionId
     * @param authorization
     * @return
     * @throws AuthorizationFailedException
     * @throws InvalidQuestionException     and AnswerNotFoundException
     */
    @RequestMapping(method = RequestMethod.GET, path = "/answer/all/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<?> getAllAnswersToQuestion(@PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = authorizationService.isValidActiveAuthToken(authorization, ActionType.GET_ALL_ANSWER_TO_QUESTION);
        List<Answer> answerList = answerService.getAnswersForQuestion(questionId);
        StringBuilder contentBuilder = new StringBuilder();
        getContentsString(answerList, contentBuilder);
        StringBuilder uuIdBuilder = new StringBuilder();
        String questionContentValue = getUuIdStringAndQuestionContent(answerList, uuIdBuilder);
        //get all answer to particular question with respect to question id
        AnswerDetailsResponse response = new AnswerDetailsResponse()
                .id(uuIdBuilder.toString())
                .answerContent(contentBuilder.toString())
                .questionContent(questionContentValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * method for appending the uuid of answers.
     *
     * @param answerList  List of questions
     * @param uuIdBuilder StringBuilder object
     *                    returns questionContent
     */
    public static final String getUuIdStringAndQuestionContent(List<Answer> answerList, StringBuilder uuIdBuilder) {
        String questionContent = new String();
        for (Answer answerObject : answerList) {
            uuIdBuilder.append(answerObject.getUuid()).append(",");
            questionContent = answerObject.getQuestion().getContent();
        }
        return questionContent;
    }

    /**
     * method for providing contents string in appended format
     *
     * @param answerList list of questions
     * @param builder    StringBuilder with appended content list.
     */
    public static final StringBuilder getContentsString(List<Answer> answerList, StringBuilder builder) {
        for (Answer answerObject : answerList) {
            builder.append(answerObject.getAnswer()).append(",");
        }
        return builder;
    }

}
