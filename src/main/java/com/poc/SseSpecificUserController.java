package com.poc;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "sse")
public class SseSpecificUserController {

    public Map<String, SseEmitter> emitters = new HashMap<>();

    // method for client subscription
    @CrossOrigin
    @GetMapping("/subscribeSpecificUser")
    public SseEmitter subscribe(@RequestParam String userID) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE); // durée de vie de l'émetteur
        sendInitEvent(sseEmitter);
        emitters.put(userID, sseEmitter);

        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        sseEmitter.onTimeout(() -> emitters.remove(sseEmitter));
        sseEmitter.onError(e -> emitters.remove(sseEmitter));

        return sseEmitter;
    }

    // Dispatching events to a Specific User
    @PostMapping("/dispatchEventToSpecificUser")
    public void dispatchEventToClients(@RequestParam String title, @RequestParam String text, @RequestParam String userID) {
        String eventFormatted = new JSONObject()
                .put("title", title)
                .put("text", text).toString();

        SseEmitter sseEmitter = emitters.get(userID);
        if (sseEmitter != null) {
            try {
                sseEmitter.send(SseEmitter.event().name("latestNews").data(eventFormatted));
            } catch (Exception e) {
                emitters.remove(sseEmitter);
            }
        }
    }

    private void sendInitEvent(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().name("INIT"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
