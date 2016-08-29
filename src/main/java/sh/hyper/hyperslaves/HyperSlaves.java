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
import hudson.Plugin;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.model.Items;
import hudson.model.Job;
import hudson.slaves.Cloud;
import sh.hyper.hyperslaves.drivers.PlainHyperAPIContainerDriverFactory;
import sh.hyper.hyperslaves.spi.ContainerDriverFactory;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link Cloud} implementation designed to launch a container to establish a Jenkins executor.
 */
public class HyperSlaves extends Plugin implements Describable<HyperSlaves> {

    private ContainerDriverFactory containerDriverFactory;

    public HyperSlaves() {
        init();
    }


    private Object readResolve() {
        init();
        return this;
    }

    private void init() {
        if (this.containerDriverFactory == null) {
            containerDriverFactory = new PlainHyperAPIContainerDriverFactory();
        }
    }

    public void start() throws IOException {
        load();
    }

    @Override
    public void configure(StaplerRequest req, JSONObject formData) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, formData);
        save();
    }

    public HyperProvisionerFactory createStandardJobProvisionerFactory(Job job) throws IOException, InterruptedException {
        // TODO iterate on job's ItemGroup and it's parents so end-user can configure this at folder level.

        return new HyperProvisionerFactory.StandardJob(containerDriverFactory.forJob(job), job);
    }

    public static HyperSlaves get() {
        HyperSlaves slaves = null;
        try {
            slaves = Jenkins.getInstance().getPlugin(HyperSlaves.class);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Jenkins.getInstance().getPlugin() is null, get a NullPointerException.");
        }
        return slaves;
    }

    @Override
    public Descriptor<HyperSlaves> getDescriptor() {
        Descriptor<HyperSlaves> slaves = null;
        try {
            slaves = Jenkins.getInstance().getDescriptorOrDie(HyperSlaves.class);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Jenkins.getDescriptor().getDescriptorOrDie() is null, get a NullPointerException.");
        }
        return slaves;
    }


    static {
        Items.XSTREAM2.aliasPackage("xyz.quoidneufdocker.jenkins", "it.dockins");
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<HyperSlaves> {

        @Override
        public String getDisplayName() {
            return "Hyper Slaves";
        }
    }
}
