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

import hudson.model.BuildBadgeAction;

public class JobBuildsContainersContext implements BuildBadgeAction {

    protected String workdirVolume;

    protected ContainerInstance slaveContainer;

    public JobBuildsContainersContext() {

    }

    public String getWorkdirVolume() {
        return workdirVolume;
    }

    public void setWorkdirVolume(String workdirVolume) {
        this.workdirVolume = workdirVolume;
    }

    public ContainerInstance getSlaveContainer() {
        return slaveContainer;
    }

    public void setSlaveContainer(ContainerInstance slaveContainer) {
        this.slaveContainer = slaveContainer;
    }

    @Override
    public String getIconFileName() {
        return "/plugin/hyper-slaves/images/24x24/hyper-logo.png";
    }

    @Override
    public String getDisplayName() {
        return "Docker Build Context";
    }

    @Override
    public String getUrlName() {
        return "docker";
    }
}
