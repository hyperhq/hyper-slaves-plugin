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

package sh.hyper.hyperslaves.drivers;

import hudson.Extension;
import hudson.model.Job;
import hudson.util.ListBoxModel;
import sh.hyper.hyperslaves.spi.ContainerDriver;
import sh.hyper.hyperslaves.spi.ContainerDriverFactory;
import sh.hyper.hyperslaves.spi.ContainerDriverFactoryDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class PlainHyperAPIContainerDriverFactory extends ContainerDriverFactory {

    private final CLIENT client;


    @DataBoundConstructor
    public PlainHyperAPIContainerDriverFactory(CLIENT client) {
        this.client = client;
    }

    public PlainHyperAPIContainerDriverFactory() {
        this(CLIENT.HYPER_CLI);
    }

    public CLIENT getClient() {
        return client;
    }

    @Override
    public ContainerDriver forJob(Job context) throws IOException, InterruptedException {
        return client.forDockerHost();
    }

    public enum CLIENT {
        HYPER_CLI {
            String getDisplayName() {
                return "Hyper CLI (require cli executable on PATH)";
            }

            ContainerDriver forDockerHost() throws IOException, InterruptedException {
                return new CliHyperDriver();
            }
        };

        abstract String getDisplayName();

        abstract ContainerDriver forDockerHost() throws IOException, InterruptedException;
    }

    @Extension
    public static class DescriptorImp extends ContainerDriverFactoryDescriptor {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Use plain Container API";
        }

        public ListBoxModel doFillClientItems() {
            final ListBoxModel options = new ListBoxModel();
            for (CLIENT client : CLIENT.values()) {
                options.add(client.getDisplayName(), client.name());
            }
            return options;
        }
    }

}
