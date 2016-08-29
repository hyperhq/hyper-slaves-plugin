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

package sh.hyper.hyperslaves.spec;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.Descriptor;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import hudson.util.ListBoxModel.Option;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import sh.hyper.hyperslaves.spi.ContainerDriver;

import java.io.IOException;

public class ImageIdContainerDefinition extends ContainerDefinition {

    private final String image;

    private final String size;

    private final boolean forcePull;

    @DataBoundConstructor
    public ImageIdContainerDefinition(String image, String size, boolean forcePull) {
        this.image = image;
        this.size = size;
        this.forcePull = forcePull;
    }

    @Override
    public String getSize() {
        return size;
    }

    public String getImage() {
        return image;
    }


    @Override
    public String getImage(ContainerDriver driver, TaskListener listener) throws IOException, InterruptedException {

        boolean pull = forcePull;
        final Launcher launcher = new Launcher.LocalLauncher(listener);
        boolean result = driver.checkImageExists(launcher, image);

        if (!result) {
            // Could be a hyper failure, but most probably image isn't available
            pull = true;
        }

        if (pull) {
            listener.getLogger().println("Pulling docker image " + image);
            driver.pullImage(launcher, image);
        }

        return image;
    }

    @Extension(ordinal = 99)
    public static class DescriptorImpl extends Descriptor<ContainerDefinition> {

        @Override
        public String getDisplayName() {
            return "Docker image";
        }

        public ListBoxModel doFillSizeItems(@QueryParameter String size) {
            return new ListBoxModel(
                    new Option("S1 $0.0000004/sec ($0.00144/hour): 64MB Mem, 1 CPU Core, 10GB Disk", "s1", size.matches("s1")),
                    new Option("S2 $0.0000006/sec ($0.00216/hour): 128MB Mem, 1 CPU Core, 10GB Disk", "s2", size.matches("s2")),
                    new Option("S3 $0.000001/sec ($0.0036/hour): 256MB Mem, 1 CPU Core, 10GB Disk", "s3", size.matches("s3")),
                    new Option("S4 $0.000002/sec ($0.0072/hour): 512MB Mem, 1 CPU Core, 10GB Disk", "s4", size.matches("s4")),
                    new Option("M1 $0.000004/sec ($0.0144/hour): 1GB Mem, 1 CPU Core, 10GB Disk", "m1", size.matches("m1")),
                    new Option("M2 $0.000008/sec ($0.0288/hour): 2GB Mem, 2 CPU Core, 10GB Disk", "m2", size.matches("m2")),
                    new Option("M3 $0.000015/sec ($0.054/hour): 4GB Mem, 2 CPU Core, 10GB Disk", "m3", size.matches("m3")),
                    new Option("L1 $0.00003/sec ($0.108/hour): 4GB Mem, 4 CPU Core, 10GB Disk", "l1", size.matches("l1")),
                    new Option("L2 $0.00006/sec ($0.216/hour): 8GB Mem, 4 CPU Core, 10GB Disk", "l2", size.matches("l2")),
                    new Option("L3 $0.00012/sec ($0.432/hour): 16GB Mem, 8 CPU Core, 10GB Disk", "l3", size.matches("l3"))
            );
        }
    }
}
