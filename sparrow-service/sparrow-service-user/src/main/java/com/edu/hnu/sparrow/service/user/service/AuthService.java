package com.edu.hnu.sparrow.service.user.service;

import com.edu.hnu.sparrow.common.entity.AuthToken;

import javax.servlet.http.HttpServletResponse;

public interface AuthService {

    AuthToken login(String username, String password, HttpServletResponse httpServletResponse);
}
