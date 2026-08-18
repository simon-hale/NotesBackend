package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.service.directory.UpdateDirectoryInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UpdateDirectoryInfoServiceImpl implements UpdateDirectoryInfoService {

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject modifyDirectoryNameById(String idString, String name, String language) {
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
        if (name.equals("root") || name.equals("root_parent")) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名不允许"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "This name is not allowed.");
            }
            return resp;
        }
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
                    resp.put("error_message", "目标目录查询出错。");
                    break;
                case LanguagesSelector.en_US:
                default:
                    resp.put(
                            "error_message",
                            "Error querying the target directory."
                    );
            }
            return resp;
        }
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目标目录不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Target directory does not exist.");
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
                case LanguagesSelector.zh_CN: resp.put("error_message", "系统根目录不能修改"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Cannot modify the system root directory.");
            }
            return resp;
        }
        String oldName = directory.getName();
        if (oldName.equals(name)) {
            resp.put("error_message", "success");
            return resp;
        }
        int parentId = directory.getParentId();
        Long countOld;
        Long countNew;
        try {
            countOld = directoryMapper.selectCount(new QueryWrapper<Directory>()
                    .eq("parent_id", parentId)
                    .eq("name", oldName)
                    .eq("user_id", userId));
            countNew = directoryMapper.selectCount(new QueryWrapper<Directory>()
                    .eq("parent_id", parentId)
                    .eq("name", name)
                    .eq("user_id", userId));
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录数量查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying directory count.");
            }
            return resp;
        }
        if (countOld != 1) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "旧目录数量不为1"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "The number of old directories is not one.");
            }
            return resp;
        }
        if (countNew != 0) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名在当前目录中已存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "This name already exists in current directory.");
            }
            return resp;
        }
        int modifySuccess;
        try {
            modifySuccess = directoryMapper.update(
                    null,
                    new UpdateWrapper<Directory>()
                            .eq("id", id)
                            .eq("user_id", userId)
                            .set("name", name)
            );
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库操作错误"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database operation error.");
            }
            return resp;
        }

        resp.put(
                "error_message",
                modifySuccess == 1 ? "success" : switch (language) {
                    case LanguagesSelector.zh_CN -> "数据库更新返回值非1";
                    case LanguagesSelector.en_US -> "Database update returned a non-1 value.";
                    default -> "Database update returned a non-1 value.";
                }
        );
        return resp;
    }
}
