package org.radrso.workflow.wfservice.service.exec;

import lombok.extern.log4j.Log4j;
import org.radrso.plugins.FileUtils;
import org.radrso.plugins.requests.entity.exceptions.ResponseCode;
import org.radrso.workflow.ConfigConstant;
import org.radrso.workflow.entities.config.WorkflowConfig;
import org.radrso.workflow.entities.config.items.Step;
import org.radrso.workflow.entities.exceptions.WFRuntimeException;
import org.radrso.workflow.entities.response.WFResponse;
import org.radrso.workflow.entities.wf.WorkflowErrorLog;
import org.radrso.workflow.entities.wf.WorkflowInstance;
import org.radrso.workflow.resolvers.WorkflowResolver;
import org.radrso.workflow.rmi.WorkflowCommander;
import org.radrso.workflow.rmi.WorkflowInstanceExecutor;
import org.radrso.workflow.wfservice.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * Created by raomengnan on 17-1-19.
 */
@Service
@Log4j
public class WorkflowCommandServiceImpl implements WorkflowCommandService{
    public static final String ROOT = ConfigConstant.SERVICE_JAR_HOME;

    @Autowired
    protected WorkflowInstanceExecutor workflowInstanceExecutor;
    @Autowired
    protected WorkflowCommander workflowCommander;

    @Autowired
    protected WorkflowService workflowService;
    @Autowired
    protected WorkflowExecuteStatusService workflowExecuteStatusService;
    @Autowired
    protected WorkflowInstanceService workflowInstanceService;
    @Autowired
    protected WorkflowLogService workflowLogService;


    @Override
    public boolean haveJarsDefine(String workflowId){
        WorkflowConfig workflowConfig = workflowService.getByWorkflowId(workflowId);
        if(workflowConfig == null)
            return false;
        List<String> jars = workflowConfig.getJars();
        if(jars == null || jars.size() == 0)
            return false;
        return true;
    }

    @Override
    public void importJars(String workflowId) {
        WorkflowConfig workflowConfig = workflowService.getByWorkflowId(workflowId);
        String app = workflowConfig.getApplication();
        String jarsRoot = ROOT + app + File.separator ;
        List<String> jars = workflowConfig.getJars();
        if(jars == null)
            return;

        jars.forEach(j->{
            File jarFile = new File(jarsRoot + j);
            if(!jarFile.exists())
                throw new WFRuntimeException(WFRuntimeException.JAR_FILE_NO_FOUND + String.format("[%s]", jarsRoot + j));

            WFResponse response = workflowCommander.checkAndImportJar(app, j);
            if(response.getCode() == ResponseCode.JAR_FILE_NOT_FOUND.code()) {
                log.info(String.format("UPLOAD Local JAR[%s]", app + "/" + j));
                response = workflowCommander.importJar(app, j, FileUtils.getByte(jarFile));
            }else
                log.info(String.format("NEEDN'T UPLOAD FILE[%s]", app + "/" + j));

            if(response.getCode() / 100 != 2)
                throw new WFRuntimeException("Jar file upload failed:" + response.getMsg());
        });
    }

    @Override
    public boolean logError(WorkflowErrorLog log){
        return workflowLogService.save(log);
    }

    @Override
    public boolean updateInstance(WorkflowInstance instance){
        return workflowInstanceService.save(instance);
    }

    @Override
    public WFResponse execute(Step step, Object[] params, String[] paramNames){
        return workflowInstanceExecutor.execute(step, params, paramNames);
    }

    @Override
    public String getWFStatus(String workflowId){
        WorkflowConfig workflowConfig = workflowService.getByWorkflowId(workflowId);
        if(workflowConfig == null)
            return null;
        workflowService.updateServiceStatus(workflowConfig);
        return workflowExecuteStatusService.getStatus(workflowId);
    }

    @Override
    public WorkflowResolver branchInstance(String instanceId){
        WorkflowInstance instance = workflowInstanceService.getByInstanceId(instanceId);
        if(instance == null)
            return null;
        WorkflowConfig config = workflowService.getByWorkflowId(instance.getWorkflowId());
        WorkflowInstance newInstance = new WorkflowInstance(config.getId(), instanceId);
        return new WorkflowResolver(config, newInstance);
    }
}
