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

import hudson.model.Job;
import sh.hyper.hyperslaves.spi.ContainerDriver;
import sh.hyper.hyperslaves.spec.ContainerSetDefinition;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.slaves.SlaveComputer;

import java.io.IOException;

/**
 * Provision {@link ContainerInstance}s based on ${@link ContainerSetDefinition} to provide a queued task
 * an executor.
 */
public class HyperProvisioner {

    protected final JobBuildsContainersContext context;

    protected final TaskListener slaveListener;

    protected final ContainerDriver driver;

    protected final Launcher launcher;

    protected final ContainerSetDefinition spec;

    public HyperProvisioner(JobBuildsContainersContext context, TaskListener slaveListener, ContainerDriver driver, Job job, ContainerSetDefinition spec) throws IOException, InterruptedException {
        this.context = context;
        this.slaveListener = slaveListener;
        this.driver = driver;
        this.launcher = new Launcher.LocalLauncher(slaveListener);
        this.spec = spec;
    }

    public JobBuildsContainersContext getContext() {
        return context;
    }

    public void prepareAndLaunchSlaveContainer(final SlaveComputer computer, TaskListener listener) throws IOException, InterruptedException {
        // if slave container already exists, we reuse it
        if (context.getSlaveContainer() != null) {
            if (driver.hasContainer(launcher, context.getSlaveContainer().getId())) {
                return;
            }
        }

        String buildImage = spec.getBuildHostImage().getImage(driver, listener);
        final ContainerInstance slaveContainer = driver.createAndLaunchSlaveContainer(computer, launcher, buildImage);
        context.setSlaveContainer(slaveContainer);
    }

    public Proc launchBuildProcess(Launcher.ProcStarter procStarter, TaskListener listener) throws IOException, InterruptedException {
        return driver.execInSlaveContainer(launcher, context.getSlaveContainer().getId(), procStarter);
    }

    public void clean() throws IOException, InterruptedException {
        if (context.getSlaveContainer() != null) {
            driver.removeContainer(launcher, context.getSlaveContainer());
        }
    }
}