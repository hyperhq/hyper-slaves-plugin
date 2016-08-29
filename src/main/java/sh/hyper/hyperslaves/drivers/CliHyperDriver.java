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

import hudson.Launcher;
import hudson.Proc;
import hudson.slaves.SlaveComputer;
import hudson.util.ArgumentListBuilder;
import sh.hyper.hyperslaves.ContainerInstance;
import sh.hyper.hyperslaves.ProvisionQueueListener;
import sh.hyper.hyperslaves.spi.ContainerDriver;
import org.apache.commons.lang.StringUtils;
import jenkins.model.Jenkins;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CliHyperDriver implements ContainerDriver {

    private final boolean verbose;

    public CliHyperDriver() throws IOException, InterruptedException {
        verbose = true;
    }

    @Override
    public boolean hasContainer(Launcher launcher, String id) throws IOException, InterruptedException {
        if (StringUtils.isEmpty(id)) {
            return false;
        }

        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("inspect", "-f", "'{{.Id}}'", id);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int status = launchHyperCLI(launcher, args)
                .stdout(out).stderr(launcher.getListener().getLogger()).join();

        if (status != 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public ContainerInstance createAndLaunchSlaveContainer(final SlaveComputer computer, Launcher launcher, String image, String size) throws IOException, InterruptedException {
        String sizeFlag = "--size=" + size;
        String rootUrl = Jenkins.getInstance().getRootUrl();
        String jenkinsUrl = rootUrl + "/jnlpJars/slave.jar";
        String downloadSlaveJarCmd = String.format("which wget >/dev/null 2>&1 && wget %s -O slave.jar || (which curl && curl %s -O || echo skip download slave.jar)", jenkinsUrl, jenkinsUrl);
        String jnlpConnectCmd = String.format("java -jar slave.jar -jnlpUrl %s/%s/slave-agent.jnlp -secret %s", rootUrl, computer.getUrl(), computer.getJnlpMac());
        String startCmd = "/bin/sh -c '" + downloadSlaveJarCmd + " ; " + jnlpConnectCmd + "'";
        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("run", "-d")
                .add(sizeFlag)
                //.add("--workdir", HyperSlave.SLAVE_ROOT)
                .add(image)
                .add("/bin/sh", "-c")
                .add(startCmd);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int status = launchHyperCLI(launcher, args)
                .stdout(out).stderr(launcher.getListener().getLogger()).join();

        String containerId = out.toString("UTF-8").trim();

        if (status != 0) {
            throw new IOException("Failed to create remoting container");
        }

        return new ContainerInstance(image, containerId);
    }

    @Override
    public Proc execInSlaveContainer(Launcher launcher, String containerId, Launcher.ProcStarter starter) throws IOException, InterruptedException {
        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("exec", containerId);

        args.add("env").add(starter.envs());

        List<String> originalCmds = starter.cmds();
        boolean[] originalMask = starter.masks();
        for (int i = 0; i < originalCmds.size(); i++) {
            boolean masked = originalMask == null ? false : i < originalMask.length ? originalMask[i] : false;
            args.add(originalCmds.get(i), masked);
        }

        Launcher.ProcStarter procStarter = launchHyperCLI(launcher, args);

        if (starter.stdout() != null) {
            procStarter.stdout(starter.stdout());
        }

        return procStarter.start();
    }

    @Override
    public int removeContainer(Launcher launcher, ContainerInstance instance) throws IOException, InterruptedException {
        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("rm", "-f", "-v", instance.getId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int status = launchHyperCLI(launcher, args)
                .stdout(out).stderr(launcher.getListener().getLogger()).join();

        return status;
    }

    private static final Logger LOGGER = Logger.getLogger(ProvisionQueueListener.class.getName());

    @Override
    public void pullImage(Launcher launcher, String image) throws IOException, InterruptedException {
        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("pull")
                .add(image);

        int status = launchHyperCLI(launcher, args)
                .stdout(launcher.getListener().getLogger()).join();

        if (status != 0) {
            throw new IOException("Failed to pull image " + image);
        }
    }

    @Override
    public boolean checkImageExists(Launcher launcher, String image) throws IOException, InterruptedException {
        ArgumentListBuilder args = new ArgumentListBuilder()
                .add("inspect")
                .add("-f", "'{{.Id}}'")
                .add(image);

        return launchHyperCLI(launcher, args)
                .stdout(launcher.getListener().getLogger()).join() == 0;
    }

    public void prependDockerArgs(ArgumentListBuilder args) {
        args.prepend("docker");
    }

    public void prependHyperArgs(ArgumentListBuilder args) {
        String jenkinsHome = Jenkins.getInstance().getRootDir().getPath();
        String hyperCliPath = jenkinsHome + "/bin/hyper";
        String configPath = jenkinsHome + "/.hyper";
        args.prepend(configPath);
        args.prepend("--config");
        args.prepend(hyperCliPath);
    }

    private Launcher.ProcStarter launchHyperCLI(Launcher launcher, ArgumentListBuilder args) {
        prependHyperArgs(args);
        //prependDockerArgs(args);

        return launcher.launch()
                .cmds(args)
                .quiet(!verbose);
    }
}
