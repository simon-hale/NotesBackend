package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.service.directory.GetDirectoryInfoService;
import org.projects.backend.utils.AccessTokenExtractor;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Service
public class GetDirectoryInfoServiceImpl implements GetDirectoryInfoService {

    @Autowired
    private DirectoryMapper directoryMapper;

    @Autowired
    private FileMapper fileMapper;

    @Autowired
    private AccessTokenExtractor accessTokenExtractor;

    @Override
    public JSONObject getAllDirectoryInfoByInit(String language) {
        JSONObject resp = new JSONObject();
        Integer userId = accessTokenExtractor.getCurrentUserId();
        QueryWrapper<Directory> queryWrapperRootDir = new QueryWrapper<>();
        Directory directoryRoot;
        QueryWrapper<Directory> queryWrapperDirectory = new QueryWrapper<>();
        QueryWrapper<File> queryWrapperFile = new QueryWrapper<>();
        try{
            queryWrapperRootDir.eq("user_id", userId);
            queryWrapperRootDir.eq("name", "root");
            directoryRoot = directoryMapper.selectOne(queryWrapperRootDir);
            if (directoryRoot == null) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "根目录查询结果为空"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Root directory query is null.");
                }
                return resp;
            }
            queryWrapperDirectory.eq("parent_id", directoryRoot.getId());
            queryWrapperDirectory.eq("user_id", userId);
            List<Directory> directoryList = directoryMapper.selectList(queryWrapperDirectory);
            List<JSONObject> directories = new LinkedList<>();
            for (Directory directory : directoryList) {
                JSONObject item = new JSONObject();
                item.put("id", directory.getId());
                item.put("name", directory.getName());
                directories.add(item);
            }
            resp.put("directories", directories);
            queryWrapperFile.eq("parent_id", directoryRoot.getId());
            queryWrapperFile.eq("user_id", userId);
            List<File> fileList = fileMapper.selectList(queryWrapperFile);
            List<JSONObject> files = new LinkedList<>();
            for (File file : fileList) {
                JSONObject item = new JSONObject();
                item.put("id", file.getId());
                item.put("name", file.getName());
                item.put("creation_time", file.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("last_modified_time", file.getLastModifiedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("type", file.getType());
                files.add(item);
            }
            resp.put("files", files);
            resp.put("root_id", directoryRoot.getId());
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database query error.");
            }
            return resp;
        }
        resp.put("error_message", "success");
        return resp;
    }

    @Override
    public JSONObject getAllDirectoryInfoByParentId(String idString, String language) {
        JSONObject resp = new JSONObject();
        Integer userId = accessTokenExtractor.getCurrentUserId();
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
        Integer parentId;
        try {
            parentId = Integer.valueOf(idString.trim());
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
        Directory directoryVerify;
        QueryWrapper<Directory> queryWrapperDirectory = new QueryWrapper<>();
        QueryWrapper<File> queryWrapperFile = new QueryWrapper<>();
        try {
            directoryVerify = directoryMapper.selectById(parentId);
            if (directoryVerify == null) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "目录不存在"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Directory does not exist.");
                }
                return resp;
            }
            if (!Objects.equals(userId, directoryVerify.getUserId())) {
                switch (language) {
                    case LanguagesSelector.zh_CN: resp.put("error_message", "未授权的操作"); break;
                    case LanguagesSelector.en_US:
                    default: resp.put("error_message", "Unauthorized operation.");
                }
                return resp;
            }
            queryWrapperDirectory.eq("parent_id", parentId);
            queryWrapperDirectory.eq("user_id", userId);
            List<Directory> directoryList = directoryMapper.selectList(queryWrapperDirectory);
            List<JSONObject> directories = new LinkedList<>();
            for (Directory directory : directoryList) {
                JSONObject item = new JSONObject();
                item.put("id", directory.getId());
                item.put("name", directory.getName());
                directories.add(item);
            }
            resp.put("directories", directories);
            queryWrapperFile.eq("parent_id", parentId);
            queryWrapperFile.eq("user_id", userId);
            List<File> fileList = fileMapper.selectList(queryWrapperFile);
            List<JSONObject> files = new LinkedList<>();
            for (File file : fileList) {
                JSONObject item = new JSONObject();
                item.put("id", file.getId());
                item.put("name", file.getName());
                item.put("creation_time", file.getCreationTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("last_modified_time", file.getLastModifiedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                item.put("type", file.getType());
                files.add(item);
            }
            resp.put("files", files);
        } catch (Exception e) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "数据库查询出错"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Database query error.");
            }
            return resp;
        }
        resp.put("error_message", "success");
        return resp;
    }
}
