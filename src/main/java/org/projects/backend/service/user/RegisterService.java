package org.projects.backend.service.user;

import java.util.Map;

public interface RegisterService {
    Map<String, String> registerAccount(String username, String password, String confirmedPassword, String language);
}
