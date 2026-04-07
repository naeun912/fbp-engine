package com.fbp.engine.core;

import com.fbp.engine.message.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.Queue;

@Getter
@Setter
@ToString
public class Connection {
    private final Queue<Message> buffer;
    private InputPort target;
    private final String id;

    public Connection(Queue<Message> buffer, InputPort target, String id) {
        this.buffer = buffer;
        this.target = target;
        this.id = id;
    }

    public void deliver(Message message){
        buffer.offer(message);

        if (target != null) {
            Message msg = buffer.poll();
            if (msg != null) {
                target.receive(msg);
            }
        }
    }

    public int getBufferSize(){
        return buffer.size();
    }

}
