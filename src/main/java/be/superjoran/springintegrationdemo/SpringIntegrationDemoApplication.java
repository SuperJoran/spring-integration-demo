package be.superjoran.springintegrationdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ChannelInterceptor;

import java.io.File;
import java.util.Collections;

@SpringBootApplication
@EnableIntegration
public class SpringIntegrationDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationDemoApplication.class, args);
	}

	@Bean
	@InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
    @Autowired
	public MessageSource<File> fileReadingMessageSource(@Value("${input.directory}") String inputDirectory) {
		FileReadingMessageSource sourceReader= new FileReadingMessageSource();
		sourceReader.setDirectory(new File(inputDirectory));
		return sourceReader;
	}


    @Bean
    public MessageChannel fileChannel(ChannelInterceptor countingChannelInterceptor) {
        DirectChannel channel = new DirectChannel();
        channel.setInterceptors(Collections.singletonList(countingChannelInterceptor));
        return channel;
    }

    @Router(inputChannel = "fileChannel")
    public String route(File payload) {
	    String route = "nullChannel";
        String fileName = payload.getAbsolutePath().toLowerCase();
        if(fileName.endsWith(".jpg")) {
            route = "imageChannel";
        } else if(fileName.endsWith(".mov") || fileName.endsWith(".mp4") || fileName.endsWith(".mkv")) {
	        route = "movieChannel";
        }
        return route;
    }

    @Bean
    public MessageChannel imageChannel(ChannelInterceptor countingChannelInterceptor) {
        return new DirectChannel();
    }

	@Bean
	@ServiceActivator(inputChannel= "imageChannel")
    @Autowired
	public MessageHandler imageMessageHandler(@Value("${output.directory.images}") String imageDirectory) {
        return this.createFileWritingMessageHandler(imageDirectory);
	}

    @Bean
    public MessageChannel movieChannel(ChannelInterceptor countingChannelInterceptor) {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel= "movieChannel")
    @Autowired
    public MessageHandler movieMessageHandler(@Value("${output.directory.images}") String movieDirectory) {
        return this.createFileWritingMessageHandler(movieDirectory);
    }

    private MessageHandler createFileWritingMessageHandler(String directory) {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(directory));
        handler.setFileExistsMode(FileExistsMode.REPLACE);
        handler.setExpectReply(false);
        handler.setDeleteSourceFiles(true);
        return handler;
    }
}
