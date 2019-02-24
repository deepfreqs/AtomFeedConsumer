package org.deepfrequencies.atomfeedconsumer.poller;

import junit.framework.TestCase;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AtomFeedConsumerPollerTestxxx extends TestCase {
	
	@Mock
	AtomFeedConsumerPoller pollerMock;
	
	@Test
	public void testPollInternal() {
		when(pollerMock.isPollingActivated()).thenReturn(true);
		boolean check = pollerMock.isPollingActivated(); 

        assertTrue(check); 

        verify(pollerMock).isPollingActivated(); 
		
	}

}
//Mockito can only mock non-private & non-final classes. java upgrade