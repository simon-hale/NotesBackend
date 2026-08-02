package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.directory.DeleteDirectoryInfoService;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeleteDirectoryInfoServiceImpl implements DeleteDirectoryInfoService {
    @Autowired
    private DirectoryMapper directoryMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject deleteDirectoryById(Integer id, String username, String language) {
        JSONObject resp = new JSONObject();
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory does not exist.");
            }
            return resp;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "用户不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "User does not exist.");
            }
            return resp;
        }
        if (!user.getId().equals(directory.getUserId())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "权限不足"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Permission denied.");
            }
            return resp;
        }
        if("root".equals(directory.getName()) || "root_parent".equals(directory.getName())){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "系统根目录不能删除"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Cannot delete the system root directory.");
            }
            return resp;
        }
        if (!fileMapper.selectList(new QueryWrapper<File>().eq("parent_id", id)).isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory does not empty.");
            }
            return resp;
        }
        List<Directory> directoryList = directoryMapper.selectList(new QueryWrapper<Directory>().eq("parent_id", id));
        if (directoryList == null || directoryList.isEmpty()) {
            if (directoryMapper.deleteById(id) == 1) resp.put("error_message", "success");
            else resp.put("error_message", "SQL error");
        } else {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory does not empty.");
            }
        }
        return resp;
    }
}
