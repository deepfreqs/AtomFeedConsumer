package org.deepfrequencies.atomfeedconsumer.poller;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.client.utils.DateUtils;
import org.deepfrequencies.atomfeedconsumer.sqsadapter.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

@Component
public class AtomFeedConsumerPoller {

	private final Logger log = LoggerFactory.getLogger(AtomFeedConsumerPoller.class);

	private String url = "";

	private RestTemplate restTemplate = new RestTemplate();

	private Date lastModified = null;

	private boolean pollingActivated;

	@Autowired
	private MessageService messageService;


//http://www.tagesschau.de/xml/atom/
//http://rss.dw.com/atom/rss-de-all^
//https://www.heise.de/newsticker/heise-atom.xml
	
	@Autowired
	public AtomFeedConsumerPoller(@Value("${feed.url}") String url,
			@Value("${poller.activated:true}") boolean pollingActivated) {
		super();
		this.pollingActivated = pollingActivated;
		this.url = url;
		lastModified = new Date(0);
	}

	public boolean isPollingActivated() {
		return pollingActivated;
	}

	public void setPollingActivated(boolean pollingActivated) {
		this.pollingActivated = pollingActivated;
	}

	@Scheduled(fixedDelayString = "${fixedDelay.in.milliseconds}")
	public void poll() {
		if (pollingActivated) {
			pollInternal();
		}
	}

	public void pollInternal() {
		log.trace("pollInternal");
		HttpHeaders requestHeaders = new HttpHeaders();
		if (lastModified != null) {
			requestHeaders.set(HttpHeaders.IF_MODIFIED_SINCE, DateUtils.formatDate(lastModified));
		}	
		HttpEntity<?> requestEntity = new HttpEntity(requestHeaders);
		List<Charset> li = new ArrayList<Charset>();
		li.add(Charset.forName("UTF-8"));
		requestHeaders.setAcceptCharset(li);
		ResponseEntity<String>  res = restTemplate.getForEntity(url, String.class);
		ResponseEntity<Feed> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Feed.class);

		//etag not used by Heise, therefore null
		String etag = response.getHeaders().getFirst(HttpHeaders.ETAG);
		log.trace("etag = " + etag);
		Date fromHeader = DateUtils.parseDate(response.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED));
		Date feedModified = response.getBody().getModified();
		log.trace("feedModified:" + feedModified.toString());
		if (response.getStatusCode() != HttpStatus.OK)
			log.trace("HttpStatus: " + response.getStatusCode());
		if (feedModified.after(lastModified)) {
			log.trace("data has been modified");
			Feed feed = response.getBody();
			for (Entry entry : feed.getEntries()) {
				if (entry.getUpdated().after(lastModified)) {
					ObjectMapper mapper = new ObjectMapper(); 
					try {
						log.trace(mapper.writeValueAsString(entry));
						// put entry in SQS queue
						messageService.sendMessage(mapper.writeValueAsString(entry));
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			lastModified = feedModified;
			log.trace("poller.lastModified:" + lastModified.toString());
		} else {
			log.trace("no new data");
		}
	}

}
