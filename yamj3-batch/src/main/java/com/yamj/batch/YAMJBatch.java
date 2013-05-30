package com.yamj.batch;

import com.yamj.common.cmdline.CmdLineException;
import com.yamj.common.cmdline.CmdLineOption;
import com.yamj.common.cmdline.CmdLineParser;
import com.yamj.common.tools.ClassTools;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class YAMJBatch {

    private static final Logger LOG = LoggerFactory.getLogger(YAMJBatch.class);

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        ClassTools.printHeader(YAMJBatch.class, LOG);
        CmdLineParser parser = getCmdLineParser();

        int status;
        try {
            parser.parse(args);

            YAMJBatch main = new YAMJBatch();
            status = main.execute(parser);
        } catch (CmdLineException cle) {
            System.err.println(cle.getMessage());
            status = 1;
        }
        System.exit(status);
    }

    private static CmdLineParser getCmdLineParser() {
        CmdLineParser parser = new CmdLineParser();
        parser.addOption(new CmdLineOption("b", "batch", "The batch parameter", true, true));
        return parser;
    }

    @SuppressWarnings("resource")
    private int execute(CmdLineParser parser) {
        int status;
        try {
            ApplicationContext applicationContext = new ClassPathXmlApplicationContext("yamj3-batch.xml");
            BatchManagement batchManagement = (BatchManagement) applicationContext.getBean("batchManagement");
            status = batchManagement.runBatch(parser);
        } catch (Exception error) {
            LOG.error("Failed to load batch configuration");
            error.printStackTrace(System.err);
            status = 2;
        }
        return status;
    }
}
