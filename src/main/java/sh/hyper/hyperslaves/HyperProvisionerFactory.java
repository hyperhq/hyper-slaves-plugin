/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 HyperHQ Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package sh.hyper.hyperslaves;

import sh.hyper.hyperslaves.spi.ContainerDriver;
import sh.hyper.hyperslaves.spec.ContainerSetDefinition;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.IOException;

public abstract class HyperProvisionerFactory {

    protected final ContainerDriver driver;
    protected final Job job;
    protected final ContainerSetDefinition spec;

    public HyperProvisionerFactory(ContainerDriver driver, Job job, ContainerSetDefinition spec) {
        this.driver = driver;
        this.job = job;
        this.spec = spec;
    }

    protected void prepareWorkspace(Job job, JobBuildsContainersContext context) {

        // TODO define a configurable volume strategy to retrieve a (maybe persistent) workspace
        // could rely on docker volume driver
        // in the meantime, we just rely on previous build's remoting container as a data volume container

        // reuse previous remoting container to retrieve workspace
        Run lastBuild = job.getBuilds().getLastBuild();
        if (lastBuild != null) {
            JobBuildsContainersContext previousContext = (JobBuildsContainersContext) lastBuild.getAction(JobBuildsContainersContext.class);
            if (previousContext != null && previousContext.getSlaveContainer() != null) {
                context.setSlaveContainer(previousContext.getSlaveContainer());
            }
        }
    }


    public abstract HyperProvisioner createProvisioner(TaskListener slaveListener) throws IOException, InterruptedException;

    public static class StandardJob extends HyperProvisionerFactory {

        public StandardJob(ContainerDriver driver, Job job) {
            super(driver, job, (ContainerSetDefinition) job.getProperty(ContainerSetDefinition.class));
        }

        @Override
        public HyperProvisioner createProvisioner(TaskListener slaveListener) throws IOException, InterruptedException {
            JobBuildsContainersContext context = new JobBuildsContainersContext();

            prepareWorkspace(job, context);

            return new HyperProvisioner(context, slaveListener, driver, job, spec);
        }
    }
}
