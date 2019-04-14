package org.deepfrequencies.atomfeedconsumer.sqsadapter;

import com.amazonaws.util.IOUtils;	
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;


@RestController
@RequestMapping("/main")
public class JmsSqsController {

	@Autowired
	private MessageService messageService;

	@RequestMapping(value = "/write", method = RequestMethod.POST)
	public void write(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
		InputStream inputStream = servletRequest.getInputStream();
		String message = IOUtils.toString(inputStream);
		messageService.sendMessage(message);
	}
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test() throws IOException {
		String message = "<br><div style='text-align:center;'>"
				+ "<h3>********** Hello World, Spring MVC Tutorial</h3>This message is coming from CrunchifyHelloWorld.java **********</div><br><br>";
		return message;
	}
}