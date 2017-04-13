package com.wei.app;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.linecorp.bot.model.event.message.TextMessageContent;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@SpringBootApplication
@LineMessageHandler
public class App {
	private static final Logger log = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	// ----- ここから -----
	@Autowired
	private LineMessagingService lineMessagingService;
	public Map<String, Object> context_store = null;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		/*
		 * System.out.println("event: " + event); final BotApiResponse
		 * apiResponse = lineMessagingService .replyMessage(new
		 * ReplyMessage(event.getReplyToken(), Collections.singletonList(new
		 * TextMessage(event.getSource().getUserId())))) .execute().body();
		 * System.out.println("Sent a message: " + apiResponse);
		 */
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
		String text = content.getText();

		log.info("Got text message from {}: {}, userID = {}", replyToken, text, event.getSource().getUserId());
		
		String watson_response = conversation(text);
		this.replyText(replyToken, watson_response);
		
		log.info("Returns echo message {}: {}", replyToken, watson_response);
		
		/*switch (text.toLowerCase()) {
		case "aaaaaa": {
			String userId = event.getSource().getUserId();
			if (userId != null) {
				Response<UserProfileResponse> response = lineMessagingService.getProfile(userId).execute();
				if (response.isSuccessful()) {
					UserProfileResponse profiles = response.body();
					this.reply(replyToken, Arrays.asList(new TextMessage("Display name: " + profiles.getDisplayName()),
							new TextMessage("Status message: " + profiles.getStatusMessage())));
				} else {
					this.replyText(replyToken, response.errorBody().string());
				}
			} else {
				this.replyText(replyToken, "Bot can't use profile API without user ID");
			}
			this.replyText(replyToken, "\\^Q^/");
			//System.out.println("\\^O^/");
			//this.replyText(replyToken, watson_response);
			break;
		}
		/*case "bye": {
			Source source = event.getSource();
			if (source instanceof GroupSource) {
				this.replyText(replyToken, "Leaving group");
				lineMessagingService.leaveGroup(((GroupSource) source).getGroupId()).execute();
			} else if (source instanceof RoomSource) {
				this.replyText(replyToken, "Leaving room");
				lineMessagingService.leaveRoom(((RoomSource) source).getRoomId()).execute();
			} else {
				this.replyText(replyToken, "Bot can't leave from 1:1 chat");
			}
			break;
		}
		case "confirm": {
			ConfirmTemplate confirmTemplate = new ConfirmTemplate("Do it?", new MessageAction("Yes", "Yes!"),
					new MessageAction("No", "No!"));
			TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "buttons": {
			String imageUrl = createUri("/static/buttons/1040.jpg");
			ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imageUrl, "My button sample", "Hello, my button",
					Arrays.asList(new URIAction("Go to line.me", "https://line.me"),
							new PostbackAction("Say hello1", "hello こんにちは"),
							new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
							new MessageAction("Say message", "Rice=米")));
			TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "carousel": {
			String imageUrl = createUri("/static/buttons/1040.jpg");
			CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(
					new CarouselColumn(imageUrl, "hoge", "fuga",
							Arrays.asList(new URIAction("Go to line.me", "https://line.me"),
									new PostbackAction("Say hello1", "hello こんにちは"))),
					new CarouselColumn(imageUrl, "hoge", "fuga",
							Arrays.asList(new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
									new MessageAction("Say message", "Rice=米")))));
			TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "imagemap":
			this.reply(replyToken,
					new ImagemapMessage(createUri("/static/rich"), "This is alt text", new ImagemapBaseSize(1040, 1040),
							Arrays.asList(
									new URIImagemapAction("https://store.line.me/family/manga/en",
											new ImagemapArea(0, 0, 520, 520)),
									new URIImagemapAction("https://store.line.me/family/music/en",
											new ImagemapArea(520, 0, 520, 520)),
									new URIImagemapAction("https://store.line.me/family/play/en",
											new ImagemapArea(0, 520, 520, 520)),
									new MessageImagemapAction("URANAI!", new ImagemapArea(520, 520, 520, 520)))));
			break;
		default:
			/*log.info("Returns echo message {}: {}", replyToken, text);
			this.replyText(replyToken, text);

			this.pushText(event.getSource().getUserId(), "台南張先生已經對您的物件送出個人履歷");
			String watson_response = conversation(text);
			this.replyText(replyToken, watson_response);
			
			break;
		}*/
	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "……";
		}
		this.reply(replyToken, new TextMessage(message));
	}

	private void pushText(@NonNull String userId, @NonNull String message) {
		if (userId.isEmpty()) {
			throw new IllegalArgumentException("userId must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "……";
		}
		this.push(userId, new TextMessage(message));
	}

	private static String createUri(String path) {

		String uri = ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();

		log.debug("uri = " + uri);

		return uri;
	}
	
	private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
                    .replyMessage(new ReplyMessage(replyToken, messages))
                    .execute();
            log.info("Sent messages: {}", apiResponse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void push(@NonNull String userId, @NonNull Message message) {
        push(userId, Collections.singletonList(message));
    }
    
    private void push(@NonNull String userId, @NonNull List<Message> messages) {
        try {
            Response<BotApiResponse> apiResponse = lineMessagingService
            .pushMessage(new PushMessage(userId, messages))
            .execute();
            log.info("Push messages: {}", apiResponse);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

	@EventMapping
	public void defaultMessageEvent(Event event) {
		System.out.println("event: " + event);
	}
	
	private String conversation(String text) {
		
		ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
		service.setUsernameAndPassword("42815d8f-725f-444e-8349-c5e342c134c3","oItvdC6Dt4td");

		MessageRequest newMessage = new MessageRequest.Builder().inputText(text).context(context_store).build();
		MessageResponse response = service.message("b3334384-ec1f-468c-8bab-e2e4dd4544e4", newMessage).execute();
		log.info("Watson's response: {}", response);
		//System.out.println(response);
		
		log.info("Watson's output: {}", response.getOutput().get("nodes_visited"));
		if (response.getOutput().get("nodes_visited").toString().replaceAll("[\\p{Ps}\\p{Pe}]", "").equals("item_search_request_confirmed_")){
			log.info("YEAH: {}", response.getOutput().get("nodes_visited"));
		}
		
		String response_text = this.formatToString(response.getText());
		context_store = response.getContext();
		log.info("Watson says: {}", response_text);
		//response_text = response_text.replaceAll("[\\p{Ps}\\p{Pe}]", "");
		return response_text;
	}
	
	private String formatToString(Object ob){
		String formatResult = null;
		formatResult = ob.toString().replaceAll("[\\p{Ps}\\p{Pe}]", "");
		return formatResult;
	}
	
}
