package com.pegasus.kafka.template;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

import java.io.IOException;
import java.util.Map;

public class NotOnlySelectDirective extends AuthDirective implements TemplateDirectiveModel {

    @Override
    public void execute(Environment env,
                        Map params,
                        TemplateModel[] loopVars,
                        TemplateDirectiveBody body) throws TemplateException, IOException {
        if (body != null &&
                (!checkPermission(Operation.SELECT)
                        || checkPermission(Operation.DELETE)
                        || checkPermission(Operation.UPDATE))) {
            body.render(env.getOut());
        }
    }
}