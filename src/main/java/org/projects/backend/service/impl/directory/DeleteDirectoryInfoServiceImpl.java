package org.projects.backend.service.impl.directory;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.projects.backend.mapper.DirectoryMapper;
import org.projects.backend.mapper.FileMapper;
import org.projects.backend.pojo.Directory;
import org.projects.backend.pojo.File;
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

    @Override
    public JSONObject deleteDirectoryById(Integer id, String language) {
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
