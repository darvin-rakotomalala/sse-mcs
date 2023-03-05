package com.poc;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping(path = "sse")
public class SseController {

    public List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /*
        method for client subscription
        il s'agit un émetteur qui nous servira par le suite à envoyer nos messages aux clients.
     */
    @CrossOrigin
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); // durée de vie de l'émetteur
        try {
            sseEmitter.send(SseEmitter.event().name("INIT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitters.remove(sseEmitter));
        sseEmitter.onError(e -> emitters.remove(sseEmitter));

        emitters.add(sseEmitter);
        return sseEmitter;
    }

    // method for dispatching events to all clients
    @PostMapping("/dispatchEvent")
    public void dispatchEventToClients(@RequestParam String title, @RequestParam String text) {
        String eventFormatted = new JSONObject()
                .put("title", title)
                .put("text", text).toString();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("latestNews")
                        .data(eventFormatted));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
