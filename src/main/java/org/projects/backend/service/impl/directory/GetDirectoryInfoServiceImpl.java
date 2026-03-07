package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.mapper.UserMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
import org.projects.backend.pojo.User;
import org.projects.backend.service.directory.GetDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

@Service
public class GetDirectoryInfoServiceImpl implements GetDirectoryInfoService {
    @Autowired
    private DirectoryMapper directoryMapper;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public JSONObject getAllDirectoryInfoByParentId(Integer parentId, String username) {
        JSONObject resp = new JSONObject();
        QueryWrapper<User> queryWrapperUser = new QueryWrapper<>();
        queryWrapperUser.eq("username", username);
        Integer userId = userMapper.selectOne(queryWrapperUser).getId();
        QueryWrapper<Directory> queryWrapperDirectory = new QueryWrapper<>();
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
        QueryWrapper<File> queryWrapperFile = new QueryWrapper<>();
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
        resp.put("error_message", "success");
        return resp;
    }

    @Override
    public JSONObject getAllDirectoryInfoByInit(String username) {
        JSONObject resp = new JSONObject();
        QueryWrapper<User> queryWrapperUser = new QueryWrapper<>();
        queryWrapperUser.eq("username", username);
        Integer userId = userMapper.selectOne(queryWrapperUser).getId();
        QueryWrapper<Directory> queryWrapperRootDir = new QueryWrapper<>();
        queryWrapperRootDir.eq("user_id", userId);
        queryWrapperRootDir.eq("name", "root");
        Directory directoryRoot = directoryMapper.selectOne(queryWrapperRootDir);
        QueryWrapper<Directory> queryWrapperDirectory = new QueryWrapper<>();
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
        QueryWrapper<File> queryWrapperFile = new QueryWrapper<>();
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
        resp.put("error_message", "success");
        return resp;
    }
}
