package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.type.ActionType;
import com.upgrad.quora.service.type.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Business logic for answer related operations

@Service
public class AnswerService {

    // Auto wired answerDao to provide an abstract interface
    @Autowired
    AnswerDao answerDao;
    // Auto wired questionDao to provide an abstract interface
    @Autowired
    QuestionDao questionDao;

    //an abstract interface for creating answer for a particular question
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer createAnswer(Answer answer) {
        return answerDao.createAnswer(answer);
    }

    //To check whether entered answer has existing UUID or not
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer getAnswerForUuId(String answerUuId) throws AnswerNotFoundException {
        Answer answer = answerDao.getAnswerForUuId(answerUuId);
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        } else {
            return answer;
        }
    }

    // Checks whether answer owner edits the answer and provides proper response to user
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer isUserAnswerOwner(String answerUuId, UserAuthTokenEntity authorizedUser, ActionType actionType) throws AnswerNotFoundException, AuthorizationFailedException {
        Answer answer = answerDao.getAnswerForUuId(answerUuId);

        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        } else if (!authorizedUser.getUser().getUuid().equals(answer.getUser().getUuid())) {
            if (ActionType.EDIT_ANSWER.equals(actionType)) {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
            } else {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
            }
        } else if ((!authorizedUser.getUser().getRole().equals(RoleType.admin)
                && !authorizedUser.getUser().getUuid().equals(answer.getUser().getUuid()))
                && ActionType.DELETE_ANSWER.equals(actionType)) {
            throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
        } else {
            return answer;
        }
    }

    //An abstract interface for editing answer
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer editAnswer(Answer answer) {
        return answerDao.editAnswer(answer);
    }

    //An abstract interface for deleting the answer
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAnswer(Answer answer) {
        answerDao.deleteAnswer(answer);
    }


    //An abstract interface for getting answer for question
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Answer> getAnswersForQuestion(String questionUuId) throws AnswerNotFoundException, InvalidQuestionException {

        Question question = questionDao.getQuestion(questionUuId);

        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }

        //throws an exception when there is no answer available for specific question uuid
        List<Answer> answerList = answerDao.getAnswersForQuestion(questionUuId);
        if (answerList == null) {
            throw new AnswerNotFoundException("OTHR-001", "No Answers available for the given question uuid");
        } else {
            return answerList;
        }
    }
}

