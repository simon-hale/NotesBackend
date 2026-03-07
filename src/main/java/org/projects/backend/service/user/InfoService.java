package org.projects.backend.service.user;

import java.util.Map;

//    因为Token会封装用户信息，反过来用户信息需要从Toekn中解析出来
//    此类是用来把Token解析处用户的信息

public interface InfoService {
    Map<String, String> getInfo();
}
