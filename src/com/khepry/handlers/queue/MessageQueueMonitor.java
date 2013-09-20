/*******************************************************************************
 * Copyright 2013 Khepry Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.khepry.handlers.queue;

import javafx.scene.control.TextArea;

import com.npstrandberg.simplemq.Message;
import com.npstrandberg.simplemq.MessageQueue;

public class MessageQueueMonitor implements Runnable {
	
	private MessageQueue messageQueue;
	private Long milliSeconds = 1000L;
	private TextArea txtAreaMessages;
	private Boolean terminateMonitoring = false;
	
	public MessageQueueMonitor() {
		
	}
	
	public MessageQueueMonitor(
			MessageQueue messageQueue) {
		initialize(messageQueue);
	}
	
	public MessageQueueMonitor(
			MessageQueue messageQueue,
			Long milliSeconds) {
		initialize(messageQueue, milliSeconds);
	}
	
	public MessageQueueMonitor(
			MessageQueue messageQueue,
			Long milliSeconds,
			TextArea txtAreaMessages) {
		initialize(messageQueue, milliSeconds, txtAreaMessages);
	}
	
	public final void initialize(
			MessageQueue messageQueue) {
		initialize(messageQueue, milliSeconds);;
	}
	
	public final void initialize(
			MessageQueue messageQueue,
			Long milliSeconds) {
		this.messageQueue = messageQueue;
		this.milliSeconds = milliSeconds;
	}
	
	public final void initialize(
			MessageQueue messageQueue,
			Long milliSeconds,
			TextArea txtAreaMessages) {
		this.messageQueue = messageQueue;
		this.milliSeconds = milliSeconds;
		this.txtAreaMessages = txtAreaMessages;
	}

	@Override
	public void run() {
		// loop while terminate monitoring is false
		while (!terminateMonitoring) {
			try {
				// sleep this thread for the
				// specified number of milliseconds
				Thread.sleep(milliSeconds);
				// output any received messages
				Message message;
				while ((message = messageQueue.receiveAndDelete()) != null) {
					if (txtAreaMessages != null) {
						if (message != null) {
							if (message.getBody() != null) {
								System.out.println(message.getBody());
								txtAreaMessages.appendText(System.lineSeparator() + message.getBody());
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace(System.err);
			} catch (IllegalMonitorStateException e) {
//				e.printStackTrace(System.err);
				break;
			}
		}
	}

	public MessageQueue getMessageQueue() {
		return messageQueue;
	}

	public void setMessageQueue(MessageQueue messageQueue) {
		this.messageQueue = messageQueue;
	}

	public Long getMilliSeconds() {
		return milliSeconds;
	}

	public void setMilliSeconds(Long milliSeconds) {
		this.milliSeconds = milliSeconds;
	}

	public Boolean getTerminateMonitoring() {
		return terminateMonitoring;
	}

	public void setTerminateMonitoring(Boolean terminateMonitoring) {
		this.terminateMonitoring = terminateMonitoring;
	}

}
