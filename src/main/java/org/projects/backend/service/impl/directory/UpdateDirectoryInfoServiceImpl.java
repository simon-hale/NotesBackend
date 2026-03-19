package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.service.directory.UpdateDirectoryInfoService;
import org.projects.backend.utils.LanguagesSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDirectoryInfoServiceImpl implements UpdateDirectoryInfoService {
    @Autowired
    private DirectoryMapper directoryMapper;
    @Override
    public JSONObject modifyDirectoryNameById(Integer id, String name, String language) {
        JSONObject resp = new JSONObject();
        if (name == null || name.isEmpty()) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目录名不能为空"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Name is null or empty.");
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
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "目标目录不存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "Directory does not exist.");
            }
            return resp;
        }
        String oldName = directory.getName();
        if (oldName.equals(name)) {
            resp.put("error_message", "success");
            return resp;
        }
        int parentId = directory.getParentId();
        Long countOld = directoryMapper.selectCount(new QueryWrapper<Directory>()
                .eq("parent_id", parentId)
                .eq("name", oldName));
        if (countOld != 1) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "旧目录数量不为1"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "The number of old directory is not one.");
            }
            return resp;
        }
        Long countNew = directoryMapper.selectCount(new QueryWrapper<Directory>()
                .eq("parent_id", parentId)
                .eq("name", name));
        if (countNew != 0) {
            switch (language) {
                case LanguagesSelector.zh_CN: resp.put("error_message", "该目录名在当前目录中已存在"); break;
                case LanguagesSelector.en_US:
                default: resp.put("error_message", "This name already exists in current directory.");
            }
            return resp;
        }
        int modifySuccess = directoryMapper.update(
                null,
                new UpdateWrapper<Directory>()
                        .eq("id", id)
                        .set("name", name)
        );
        if(modifySuccess == 1){
            resp.put("error_message", "success");
        }else{
            resp.put("error_message", "SQL error.");
        }
        return resp;
    }
}
