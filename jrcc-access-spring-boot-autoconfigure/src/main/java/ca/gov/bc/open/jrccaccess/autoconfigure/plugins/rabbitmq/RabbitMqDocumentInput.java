package ca.gov.bc.open.jrccaccess.autoconfigure.plugins.rabbitmq;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import ca.gov.bc.open.jrccaccess.autoconfigure.services.DocumentReadyHandler;
import ca.gov.bc.open.jrccaccess.libs.DocumentReadyMessage;
import ca.gov.bc.open.jrccaccess.libs.services.exceptions.DocumentMessageException;
import ca.gov.bc.open.jrccaccess.libs.services.exceptions.ServiceUnavailableException;
import ca.gov.bc.open.jrccaccess.libs.DocumentStorageProperties;

/**
 * The RabbitMqDocumentInput handles document from the rabbitMq message listener
 * 
 * @author alexjoybc
 * @since 0.4.0
 */
@Component
@ConditionalOnProperty(name="bcgov.access.input.plugin", havingValue = "rabbitmq")
public class RabbitMqDocumentInput {

	private Logger logger = LoggerFactory.getLogger(RabbitMqDocumentInput.class);

	private DocumentReadyHandler documentReadyHandler;
	
	private RabbitMqInputProperties rabbitMqInputProperties;	
	
	private final RedisStorageService redisStorageService;
	
	/**
	 * Creates a RabbitMqDocumentInput.
	 * 
	 * @param documentReadyHandler - A document ready handler.
	 */
	public RabbitMqDocumentInput(
			DocumentReadyHandler documentReadyHandler,
			RabbitMqInputProperties rabbitMqInputProperties, RedisStorageService redisStorageService) {
		this.documentReadyHandler = documentReadyHandler;
		this.rabbitMqInputProperties = rabbitMqInputProperties;	
		this.redisStorageService = redisStorageService;
	}

	/**
	 * Handles document ready messages. Retrieves the payload from temporary storage
	 * Handles retries sets in the message headers x-death.count
	 * and send to the documentReadyHandler.
	 * @param documentReadyMessage
	 * @param xDeath
	 */
	@RabbitListener(queues = "#{documentReadyQueue.getName()}")
	public void receiveMessage(DocumentReadyMessage documentReadyMessage,
			@Header(required = false, name = "x-death") Map<?, ?> xDeath)
	{

		logger.info("New Document Received {}", documentReadyMessage);
		
		if(xDeath != null && 
				(xDeath.get("count") instanceof Long) && 
				(Long)xDeath.get("count") > rabbitMqInputProperties.getRetryCount()) {	
			
			logger.error("Message has reach retry limit of {} retries", rabbitMqInputProperties.getRetryCount());
			throw new ImmediateAcknowledgeAmqpException("Message has reach retry limit.");
		}
		
		try {
			
			this.documentReadyHandler.handle("not implemented yet",
					documentReadyMessage.getTransactionInfo().getSender());
			logger.info("message successfully acknowledged");
		
		} catch (ServiceUnavailableException e) {
			
			logger.warn("Service unavailable exception, message will be put into the dead letter queue.");
			throw new AmqpRejectAndDontRequeueException(e.getCause());
		
		} catch (DocumentMessageException e) {
			
			logger.warn("Service unavailable exception, message will be put into the dead letter queue.");
			throw new AmqpRejectAndDontRequeueException(e.getCause());
		}
	}

}