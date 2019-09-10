package ca.bc.gov.open.jrccaccess.autoconfigure.plugins.rabbitmq;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name="bcgov.access.input.plugin", havingValue = "rabbitmq")
public class RabbitListenerAroundAdvice implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable{
        Message message = (Message) invocation.getArguments()[1];

        MDC.put("aaa", "aaa");
        Object result=null;
        try {
            result = invocation.proceed();
        }catch(Exception e){
            return  result;
        }finally {
            MDC.clear();
        }
        return  result;
    }
}
