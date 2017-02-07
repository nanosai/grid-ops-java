package com.nanosai.gridops.tcp;

/**
 * This class can keep track of messages that have been sent asynchronously to remote nodes.
 * The message tracker keeps a list of the messages by message id and time sent, so you
 * can search this list to see which messages were sent and when.
 */
public class TcpMessageTracker {

    public TcpMessage[] messages = null;
    public long[] messageIds = null;
    public long[] messageSentTimes = null;
    public int nextMessageIndex = 0;


    public TcpMessageTracker(int messageCount) {
        this.messages = new TcpMessage[messageCount];
        this.messageIds = new long[messageCount];
        this.messageSentTimes = new long[messageCount];
    }

    public void insertMessage(TcpMessage message, long messageId, long messageSentTime) {
        this.messages[this.nextMessageIndex] = message;
        this.messageIds[this.nextMessageIndex] = messageId;
        this.messageSentTimes[this.nextMessageIndex++] = messageSentTime;
    }

    public int findMessage(long messageId){
        for(int i = 0; i < this.nextMessageIndex; i++) {
            if(this.messageIds[i] == messageId){
                return i;
            }
        }
        return -1;
    }

    public void removeMessage(long messageId){
        int messageIndex = findMessage(messageId);
        for(int i=messageIndex; i<this.nextMessageIndex-1; i++){
            this.messages[i] = this.messages[i+1];
            this.messageIds[i] = this.messageIds[i+1];
            this.messageSentTimes[i] = this.messageSentTimes[i+1];
            this.nextMessageIndex--;
        }
    }


}
