package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.type.ActionType;
import com.upgrad.quora.service.type.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


//Business logic for question related operations

@Service
public class QuestionService {

    // Auto wired questionDao to provide an abstract interface
    @Autowired
    QuestionDao questionDao;

    // Auto wired userDao to provide an abstract interface
    @Autowired
    UserDao userDao;


    // An abstract interface used to create question instance in database.

    @Transactional(propagation = Propagation.REQUIRED)
    public Question createQuestion(Question question) {
        return questionDao.createQuestion(question);
    }


    // An abstract interface which gets questions for a authorized user

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Question> getQuestionsForUser(final String uuId) throws UserNotFoundException {
        UserEntity user = userDao.getUserById(uuId);
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        List<Question> questionList = questionDao.getAllQuestionsForUser(user.getUuid());
        return questionList;
    }

    //An abstract interfacewhich  gets all questions for any user

    @Transactional(propagation = Propagation.REQUIRED)
    public List<Question> getAllQuestions() {
        List<Question> questionList = questionDao.getAllQuestions();
        return questionList;
    }


    // An abstract interface checks whether the Question is asked by the owner

    @Transactional(propagation = Propagation.REQUIRED)
    public Question isUserQuestionOwner(String questionUuId, UserAuthTokenEntity authorizedUser, ActionType actionType) throws AuthorizationFailedException, InvalidQuestionException {
        Question question = questionDao.getQuestion(questionUuId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        } else if (!question.getUser().getUuid().equals(authorizedUser.getUser().getUuid())) {
            if (actionType.equals(ActionType.DELETE_QUESTION)) {
                if (authorizedUser.getUser().getRole().equals(RoleType.admin.toString())) {
                    return question;
                } else {
                    throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
                }

            } else {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
            }
        } else {
            return question;
        }
    }

    //An abstract interface to edit the Question

    @Transactional(propagation = Propagation.REQUIRED)
    public void editQuestion(Question question) {
        questionDao.editQuestion(question);
    }


    // An abstract interface to delete the question

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteQuestion(Question question) {
        questionDao.deleteQuestion(question);
    }


    // getting object for the specific question uuid and if the Question is not found, throws InvalidQuestionException.

    public Question getQuestionForUuId(String questionUuId) throws InvalidQuestionException {
        Question question = questionDao.getQuestion(questionUuId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        } else {
            return question;
        }
    }
}
