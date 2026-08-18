package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.service.directory.InsertDirectoryInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class InsertDirectoryInfoServiceImpl implements InsertDirectoryInfoService {

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject createDirectory(String name, String parentIdString, String language) {
        JSONObject resp = new JSONObject();
        if (name == null || name.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录名不能为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Name is null or empty.");
            }
            return resp;
        }
        name = name.trim();
        if (name.length() > 100) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录名长度不能大于100个字符"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory name max 100 chars.");
            }
            return resp;
        }
        if(name.equals("root") || name.equals("root_parent")){
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名不被允许"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "This directory name is not allowed.");
            }
            return resp;
        }
        Integer userId = accessTokenExtractor.getCurrentUserId();
        if (parentIdString == null || parentIdString.isBlank()) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录 ID 不能为空。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The parent directory ID cannot be empty."
                    );
            }
            return resp;
        }
        Integer parentId;
        try {
            parentId = Integer.valueOf(parentIdString.trim());
        } catch (NumberFormatException e) {
            switch (language) {
                case LanguagesSelector.zh_CN:
                    resp.put("error_message", "父目录 ID 格式不正确。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "The parent directory ID is invalid."
                    );
            }
            return resp;
        }
        Directory directoryParent;
        QueryWrapper<Directory> directoryQueryWrapper = new QueryWrapper<>();
        try {
            directoryParent = directoryMapper.selectById(parentId);
            if (directoryParent == null) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "父目录查询为空"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "The parent directory query result is null.");
                }
                return resp;
            }
            if (!Objects.equals(directoryParent.getUserId(), userId)) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "未授权的操作"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Unauthorized operation.");
                }
                return resp;
            }
            directoryQueryWrapper.eq("name", name);
            directoryQueryWrapper.eq("parent_id", parentId);
            directoryQueryWrapper.eq("user_id", userId);
            if (directoryMapper.selectOne(directoryQueryWrapper) != null) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名在当前目录中已存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "This name already exists in current directory.");
                }
            } else {
                Directory directory = new Directory();
                directory.setName(name);
                directory.setParentId(parentId);
                directory.setUserId(userId);

                int inserted = directoryMapper.insert(directory);

                resp.put(
                        "error_message",
                        inserted > 0 ? "success" :
                                switch (language) {
                                    case LanguagesSelector.zh_CN -> "创建失败";
                                    case LanguagesSelector.en_US -> "Insert failed.";
                                    default -> "Insert failed.";
                                }
                );
            }
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库操作出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database operation error.");
            }
        }
        return resp;
    }
}
