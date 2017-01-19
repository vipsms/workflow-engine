package org.radrso.workflow.wfservice.service.impl;

import lombok.extern.log4j.Log4j;
import org.radrso.plugins.FileUtils;
import org.radrso.workflow.entities.config.WorkflowConfig;
import org.radrso.workflow.wfservice.repositories.WorkflowRepository;
import org.radrso.workflow.wfservice.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by raomengnan on 17-1-17.
 */
@Log4j
@Service
public class WorkflowServiceImpl implements WorkflowService {
    @Autowired
    private WorkflowRepository workflowRepository;

    @Override
    public boolean save(WorkflowConfig workflowConfig) {
        if(workflowConfig == null)
            return false;
        workflowRepository.save(workflowConfig);
        return true;
    }

    @Override
    public WorkflowConfig getByWorkflowId(String workflowId) {
        if(workflowId == null)
            return null;
        return workflowRepository.findOne(workflowId);
    }

    @Override
    public List<WorkflowConfig> getByApplication(String application) {
        if(application == null)
            return new ArrayList<>();
        return workflowRepository.findByApplication(application);
    }

    @Override
    public boolean delete(String workflowId) {
        if(workflowId == null)
            return false;
        workflowRepository.delete(workflowId);
        return true;
    }

    @Override
    public boolean deleteByApplication(String application) {
        if(application == null)
            return false;
        workflowRepository.deleteByApplication(application);
        return true;
    }

    @Override
    public boolean transferJarFile(String application, MultipartFile originFile) {
        String jarRoots = FileUtils.getProjectHome() + File.separator + application + File.separator;
        String originalFileName = originFile.getOriginalFilename();
        try {
           return FileUtils.writeFile(jarRoots , originalFileName, originFile.getBytes());
        } catch (IOException e) {
            log.error(e);
            return false;
        }
    }
}