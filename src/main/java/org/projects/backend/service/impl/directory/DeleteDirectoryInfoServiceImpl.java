package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.service.directory.DeleteDirectoryInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DeleteDirectoryInfoServiceImpl implements DeleteDirectoryInfoService {

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject deleteDirectoryById(String idString, String language) {
        JSONObject resp = new JSONObject();
        if (idString == null || idString.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "目录 ID 不能为空。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The directory ID cannot be empty."
                    );
            }
            return resp;
        }
        Integer id;
        try {
            id = Integer.valueOf(idString.trim());
        } catch (NumberFormatException e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "目录 ID 格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The directory ID is invalid."
                    );
            }
            return resp;
        }
        Directory directory;
        try {
            directory = directoryMapper.selectById(id);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "目录查询出错。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Error querying the directory."
                    );
            }
            return resp;
        }
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory does not exist.");
            }
            return resp;
        }
        Integer userId = accessTokenExtractor.getCurrentUserId();
        if (!Objects.equals(userId, directory.getUserId())) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "未授权的操作"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Unauthorized operation.");
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
        List<File> fileList;
        List<Directory> directoryList;
        try {
            fileList = fileMapper.selectList(new QueryWrapper<File>().eq("parent_id", id));
            directoryList = directoryMapper.selectList(new QueryWrapper<Directory>().eq("parent_id", id));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录内容查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying directory contents.");
            }
            return resp;
        }
        if (!fileList.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory is not empty.");
            }
            return resp;
        }
        if (directoryList.isEmpty()) {
            int deleted;
            try {
                deleted = directoryMapper.delete(
                        new QueryWrapper<Directory>()
                                .eq("id", id)
                                .eq("user_id", userId)
                );
            } catch (Exception e) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "数据库操作出错"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Database operation error.");
                }
                return resp;
            }
            resp.put(
                    "error_message",
                    deleted == 1 ? "success" : switch (language) {
                        case LanguagesSelector.zh_CN -> "数据库删除返回值非1";
                        case LanguagesSelector.en_US -> "Database deletion returned a non-1 value.";
                        default -> "Database deletion returned a non-1 value.";
                    }
            );
        } else {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录不为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory is not empty.");
            }
        }
        return resp;
    }
}
