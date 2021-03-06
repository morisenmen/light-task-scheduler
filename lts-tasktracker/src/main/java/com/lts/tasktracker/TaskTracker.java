package com.lts.tasktracker;

import com.lts.core.cluster.AbstractClientNode;
import com.lts.core.constant.Constants;
import com.lts.core.constant.Level;
import com.lts.remoting.RemotingProcessor;
import com.lts.tasktracker.cmd.JobTerminateCmd;
import com.lts.tasktracker.domain.TaskTrackerAppContext;
import com.lts.tasktracker.domain.TaskTrackerNode;
import com.lts.tasktracker.monitor.StopWorkingMonitor;
import com.lts.tasktracker.monitor.TaskTrackerMStatReporter;
import com.lts.tasktracker.processor.RemotingDispatcher;
import com.lts.tasktracker.runner.JobRunner;
import com.lts.tasktracker.runner.RunnerFactory;
import com.lts.tasktracker.runner.RunnerPool;
import com.lts.tasktracker.support.JobPullMachine;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 *         任务执行节点
 */
public class TaskTracker extends AbstractClientNode<TaskTrackerNode, TaskTrackerAppContext> {

    public TaskTracker() {
        appContext.setMStatReporter(new TaskTrackerMStatReporter(appContext));
    }

    @Override
    protected void beforeStart() {
        appContext.setRemotingClient(remotingClient);
        // 设置 线程池
        appContext.setRunnerPool(new RunnerPool(appContext));
        appContext.getMStatReporter().start();
        appContext.setJobPullMachine(new JobPullMachine(appContext));
        appContext.setStopWorkingMonitor(new StopWorkingMonitor(appContext));

        appContext.getHttpCmdServer().registerCommands(
                new JobTerminateCmd(appContext));     // 终止某个正在执行的任务
    }

    @Override
    protected void afterStart() {
        if (config.getParameter(Constants.TASK_TRACKER_STOP_WORKING_ENABLE, false)) {
            appContext.getStopWorkingMonitor().start();
        }
    }

    @Override
    protected void afterStop() {
        appContext.getMStatReporter().stop();
        appContext.getStopWorkingMonitor().stop();
        appContext.getRunnerPool().shutDown();
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
    }

    public <JRC extends JobRunner> void setJobRunnerClass(Class<JRC> clazz) {
        appContext.setJobRunnerClass(clazz);
    }

    public void setWorkThreads(int workThreads) {
        config.setWorkThreads(workThreads);
    }

    /**
     * 设置业务日志记录级别
     */
    public void setBizLoggerLevel(Level level) {
        if (level != null) {
            appContext.setBizLogLevel(level);
        }
    }

    /**
     * 设置JobRunner工场类，一般用户不用调用
     */
    public void setRunnerFactory(RunnerFactory factory) {
        appContext.setRunnerFactory(factory);
    }
}
