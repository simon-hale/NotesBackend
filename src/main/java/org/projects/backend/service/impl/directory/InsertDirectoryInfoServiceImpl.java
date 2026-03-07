package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.User;
import org.projects.backend.service.directory.InsertDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InsertDirectoryInfoServiceImpl implements InsertDirectoryInfoService {
    @Autowired
    private DirectoryMapper directoryMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject createDirectory(String name, Integer parentId, String username) {
        JSONObject resp = new JSONObject();
        if (name == null || name.isEmpty()) {
            resp.put("error_message", "Name is null or empty.");
            return resp;
        }
        if(name.equals("root") || name.equals("root_parent")){
            resp.put("error_message", "This name is not allowed.");
            return resp;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        QueryWrapper<Directory> directoryQueryWrapper = new QueryWrapper<>();
        directoryQueryWrapper.eq("name", name);
        directoryQueryWrapper.eq("parent_id", parentId);
        directoryQueryWrapper.eq("user_id", user.getId());
        if (directoryMapper.selectOne(directoryQueryWrapper) != null) {
            resp.put("error_message", "This name already exists in current directory.");
        }else{
            Directory directory = new Directory();
            directory.setName(name);
            directory.setParentId(parentId);
            directory.setUserId(user.getId());
            try {
                if(directoryMapper.insert(directory) > 0) resp.put("error_message", "success");
                else resp.put("error_message", "Insert failed.");
            } catch (Exception e) {
                resp.put("error_message", "SQL error");
            }
        }
        return resp;
    }
}
