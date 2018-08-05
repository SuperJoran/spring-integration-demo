package be.superjoran.springintegrationdemo.interceptors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CountingChannelInterceptor implements ChannelInterceptor {
    private static final Logger LOG = LogManager.getLogger(CountingChannelInterceptor.class);
    private final AtomicInteger sendCount = new AtomicInteger();

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        this.sendCount.incrementAndGet();
        LOG.info(() -> String.format("I have just sent message number %d", this.sendCount.get()));
    }
}

