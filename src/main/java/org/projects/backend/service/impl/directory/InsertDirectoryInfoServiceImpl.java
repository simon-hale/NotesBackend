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
    public JSONObject createDirectory(String name, Integer parentId, String language) {
        JSONObject resp = new JSONObject();
        if (name == null || name.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录名不能为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Name is null or empty.");
            }
            return resp;
        }
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
                default: resp.put("error_message", "This name is not allowed.");
            }
            return resp;
        }
        Integer userId = accessTokenExtractor.getCurrentUserId();
        Directory directoryParent = directoryMapper.selectById(parentId);
        if (directoryParent == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "所在目录查询错误"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Error querying the current directory.");
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
        QueryWrapper<Directory> directoryQueryWrapper = new QueryWrapper<>();
        directoryQueryWrapper.eq("name", name);
        directoryQueryWrapper.eq("parent_id", parentId);
        directoryQueryWrapper.eq("user_id", userId);
        if (directoryMapper.selectOne(directoryQueryWrapper) != null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名在当前目录中已存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "This name already exists in current directory.");
            }
        }else{
            Directory directory = new Directory();
            directory.setName(name);
            directory.setParentId(parentId);
            directory.setUserId(userId);
            try {
                if(directoryMapper.insert(directory) > 0) resp.put("error_message", "success");
                else
                    switch (language) {
                        case LanguagesSelector.zh_CN: resp.put("error_message", "创建失败"); break;
                        case LanguagesSelector.en_US:
                        default: resp.put("error_message", "Insert failed.");
                    }
            } catch (Exception e) {
                resp.put("error_message", "SQL error");
            }
        }
        return resp;
    }
}
