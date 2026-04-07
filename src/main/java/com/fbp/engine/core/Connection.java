package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.LinkedBlockingQueue;

@Getter
@Setter
@ToString
public class Connection {
    private final LinkedBlockingQueue<Message> buffer;
    private final String id;


    public Connection(String id) {
        this(id, 100);

    }

    public Connection(String id, int capacity) {
        this.id = id;
        this.buffer = new LinkedBlockingQueue<>(capacity);
    }

    public void deliver(Message message) {
        try {
            buffer.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Message poll() {
        try {
            return buffer.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public int getBufferSize() {
        return buffer.size();
    }


}
