package com.example.educationalqualityproject.controller;

import com.mongodb.MongoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = {StudentController.class, TeacherController.class})
public class DirectoryDataAccessExceptionHandler {

    @ExceptionHandler({DataAccessException.class, MongoException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleMongoUnavailable(Exception exception, HttpServletRequest request, Model model) {
        String path = request.getRequestURI();
        boolean students = path.startsWith("/students");
        model.addAttribute("directoryName", students ? "estudantes" : "professores");
        model.addAttribute("retryPath", students ? "/students" : "/teachers");
        model.addAttribute("databaseConfiguration", "SPRING_DATA_MONGODB_URI ou MONGODB_URI");
        return "error/directory-unavailable";
    }
}