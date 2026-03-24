package org.example.coursework3.result;


import org.example.coursework3.entity.User;


public interface AuthResult {
    String getToken();
    User getUser();
}
