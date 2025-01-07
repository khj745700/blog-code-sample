package shop.khj745700.blog.sseemitterexample.controller;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
public class SseController {
    private final static Long SSE_CONNECTION_TIMEOUT = 5000L;
    private final Map<SseEmitter, Thread> emitters = new HashMap<>();

    @GetMapping(value = "/sse/connection", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter connection() throws IOException {
        SseEmitter emitter = createSseEmitter();
        emitter.send(createDummyData());
        emitters.get(emitter).start();
        return emitter;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/index.html";
    }

    private SseEmitter createSseEmitter(){
        SseEmitter emitter = new SseEmitter(SSE_CONNECTION_TIMEOUT);
        emitters.put(emitter, new Thread(() -> {
            int i = 1;
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    emitter.send(createSseEmitterData("info", "data sending count : " + i++));
                    Thread.sleep(1000);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }));
        setCallback(emitter);
        return emitter;
    }


    private Set<ResponseBodyEmitter.DataWithMediaType> createDummyData() {
        return createSseEmitterData("connection start","dummy data");
    }

    private Set<ResponseBodyEmitter.DataWithMediaType> createSseEmitterData(String type, String data) {
        return SseEmitter.event()
                .name(type)
                .data(data)
                .build();
    }

    private void setCallback(SseEmitter emitter) {
        emitter.onCompletion(() -> {
            emitters.get(emitter).interrupt();
            emitters.remove(emitter);
        });

        emitter.onError(ex -> {
            emitter.complete();
        });

        emitter.onTimeout(emitter::complete);
    }
}
