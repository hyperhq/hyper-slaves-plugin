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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Computer;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.slaves.EphemeralNode;
import sh.hyper.hyperslaves.api.OneShotSlave;

import java.io.IOException;

/**
 * An ${@link EphemeralNode} using hyper container to host the build processes.
 * Slave is dedicated to a specific ${@link Job}, and even better to a specific build, but when this class
 * is created the build does not yet exists due to Jenkins lifecycle.
 */
public class HyperSlave extends OneShotSlave {

    public static final String SLAVE_ROOT = "/home/jenkins/";

    private final HyperProvisionerFactory provisionerFactory;

    public HyperSlave(String name, String nodeDescription, String labelString, HyperProvisionerFactory provisionerFactory) throws Descriptor.FormException, IOException {
        // TODO would be better to get notified when the build start, and get the actual build ID. But can't find the API for that
        super(name.replaceAll("/", " » "), nodeDescription, SLAVE_ROOT, labelString, new HyperComputerLauncher());
        this.provisionerFactory = provisionerFactory;
    }

    public HyperComputer createComputer() {
        return new HyperComputer(this, provisionerFactory);
    }

    @Override
    public HyperSlave asNode() {
        return this;
    }

    @Override
    public HyperComputer getComputer() {
        return (HyperComputer) super.getComputer();
    }

    /**
     * Create a custom ${@link Launcher} which relies on plil <code>docker run</code> to start a new process
     */
    @Override
    public Launcher createLauncher(TaskListener listener) {
        HyperComputer c = getComputer();
        if (c == null) {
            listener.error("Issue with creating launcher for slave " + name + ".");
            throw new IllegalStateException("Can't create a launcher if computer is gone.");
        }

        super.createLauncher(listener);

        Launcher launcher = null;
        try {
            launcher = new HyperLauncher(listener, c.getChannel(), c.isUnix(), c.getProvisioner()).decorateFor(this);
        } catch (NullPointerException e) {
            throw new IllegalStateException("HyperLauncher is null, get a NullPointerException.");
        }
        return launcher;
    }

    /**
     * This listener get notified as the build is going to start.
     */
    @Extension
    public static class HyperSlaveRunListener extends RunListener<Run> {

        @Override
        public void onStarted(Run run, TaskListener listener) {
            Computer c = Computer.currentComputer();
            if (c instanceof HyperComputer) {
                run.addAction(((HyperComputer) c).getProvisioner().getContext());
                // TODO remove HyperSlaveAssignmentAction
            }
        }
    }
}
