package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.service.directory.UpdateDirectoryInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateDirectoryInfoServiceImpl implements UpdateDirectoryInfoService {
    @Autowired
    private DirectoryMapper directoryMapper;
    @Override
    public JSONObject modifyDirectoryNameById(Integer id, String name) {
        JSONObject resp = new JSONObject();
        if (name == null || name.isEmpty()) {
            resp.put("error_message", "Name is null or empty.");
            return resp;
        }
        if (name.equals("root") || name.equals("root_parent")) {
            resp.put("error_message", "This name is not allowed.");
            return resp;
        }
        Directory directory = directoryMapper.selectById(id);
        if (directory == null) {
            resp.put("error_message", "Directory does not exist.");
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
            resp.put("error_message", "The number of old directory is not one.");
            return resp;
        }
        Long countNew = directoryMapper.selectCount(new QueryWrapper<Directory>()
                .eq("parent_id", parentId)
                .eq("name", name));
        if (countNew != 0) {
            resp.put("error_message", "This name already exists in current directory.");
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
