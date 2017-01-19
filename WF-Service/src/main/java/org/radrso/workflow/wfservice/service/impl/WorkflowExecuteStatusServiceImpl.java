package org.radrso.workflow.wfservice.service.impl;

import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.log4j.Log4j;
import org.radrso.workflow.entities.wf.WorkflowExecuteStatus;
import org.radrso.workflow.wfservice.repositories.WorkflowStatusRepository;
import org.radrso.workflow.wfservice.service.WorkflowExecuteStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by raomengnan on 17-1-17.
 */
@Service
@Log4j
public class WorkflowExecuteStatusServiceImpl implements WorkflowExecuteStatusService{
    @Autowired
    private WorkflowStatusRepository workflowStatusRepository;

    @Override
    public WorkflowExecuteStatus get(String workflowId) {
        return workflowStatusRepository.findOne(workflowId);
    }

    @Override
    public boolean save(WorkflowExecuteStatus status) {
        if(status == null)
            return false;
        workflowStatusRepository.save(status);
        return true;
    }

    @Override
    public String getStatus(String workflowId) {
        WorkflowExecuteStatus w = workflowStatusRepository.findOne(workflowId);
        if(w != null)
            return w.getStatus();
        return null;
    }

    @Override
    public boolean deleteStatus(String workflowId) {
        if (workflowId == null)
            return false;
        workflowStatusRepository.delete(workflowId);
        return true;
    }

    @Override
    public boolean clearAll() {
        log.info("clear all");
        workflowStatusRepository.deleteAll();
        return true;
    }
}