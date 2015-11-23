package com.rebaze.autocode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rebaze.autocode.api.core.Autocode;
import com.rebaze.autocode.internal.DefaultModule;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;
import io.airlift.airline.SingleCommand;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Created by tonit on 23/11/15.
 */
@Command(name = "build", description = "build with autocode")
public class Runner
{
    @Inject
    public HelpOption helpOption;

    @Option(name = { "-c", "--config" }, description = "config folder",required=true)
    public File m_config;

    public static void main(String[] args) throws Exception {
        Runner runner = SingleCommand.singleCommand(Runner.class).parse(args);

        if (runner.helpOption.showHelpIfRequested()) {
            return;
        }

        runner.build();
    }

    private void build() throws IOException
    {
        getAutocode().build( new File(".") );
    }

    private Autocode getAutocode()
    {
        Injector injector = Guice.createInjector( new CmdModule(m_config), new DefaultModule() );
        return injector.getInstance( Autocode.class );
    }
}
