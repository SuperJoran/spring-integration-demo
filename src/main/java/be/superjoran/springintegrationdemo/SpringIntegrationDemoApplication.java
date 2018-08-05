package be.superjoran.springintegrationdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
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

	private String inputDirectory = "the_source_dir";
	private String outputDirectory = "the_dest_dir";
	private String filePattern = "*.JPG";

    @Bean
    public MessageChannel fileChannel(ChannelInterceptor countingChannelInterceptor) {
        DirectChannel channel = new DirectChannel();
        channel.setInterceptors(Collections.singletonList(countingChannelInterceptor));
        return channel;
    }

	@Bean
	@InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "1000", maxMessagesPerPoll = "1"))
	public MessageSource<File> fileReadingMessageSource() {
		FileReadingMessageSource sourceReader= new FileReadingMessageSource();
		sourceReader.setDirectory(new File(this.inputDirectory));
		sourceReader.setFilter(new SimplePatternFileListFilter(this.filePattern));
		return sourceReader;
	}

	@Bean
	@ServiceActivator(inputChannel= "fileChannel")
	public MessageHandler fileWritingMessageHandler() {
		FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(this.outputDirectory));
		handler.setFileExistsMode(FileExistsMode.REPLACE);
		handler.setExpectReply(false);
		handler.setDeleteSourceFiles(true);
		return handler;
	}
}
