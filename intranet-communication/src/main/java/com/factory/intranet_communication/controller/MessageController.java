package com.factory.intranet_communication.controller;


import com.factory.intranet_communication.model.MessageType;
import com.factory.intranet_communication.model.SenderType;
import org.springframework.web.multipart.MultipartFile;
import com.factory.intranet_communication.service.FileStorageService;
import com.factory.intranet_communication.dto.LatestMessageResponseDTO;
import com.factory.intranet_communication.dto.MessageHistoryResponseDTO;
import com.factory.intranet_communication.dto.SendMessageRequestDTO;
import com.factory.intranet_communication.model.ChannelEntity;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.factory.intranet_communication.model.MessageEntity;
import com.factory.intranet_communication.service.MessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/channels")
public class MessageController {
    private final MessageService messageService;
    private final FileStorageService fileStorageService;

    public MessageController(MessageService messageService,
                             FileStorageService fileStorageService) {
        this.messageService = messageService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/{channelId}/message")
    public ResponseEntity<String> sendMessage(
            @PathVariable Long channelId,
            @RequestBody SendMessageRequestDTO request
    ) {

        messageService.sendMessage(
                channelId,
                request.getSenderType(),
                request.getMessageType(),
                request.getTextBody(),
                request.getMediaPath()
        );

        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/{channelId}/latest")
    public ResponseEntity<LatestMessageResponseDTO> fetchLatest(
            @PathVariable Long channelId
    ) {

        ChannelEntity channel = messageService.fetchLatest(channelId);

        LatestMessageResponseDTO response = new LatestMessageResponseDTO();
        response.setStateFlag(channel.getStateFlag());

        if (channel.getLatestMessage() != null) {
            MessageEntity msg = channel.getLatestMessage();
            response.setSenderType(msg.getSenderType());
            response.setMessageType(msg.getContentType());
            response.setTextBody(msg.getTextBody());
            response.setMediaPath(msg.getMediaPath());
            response.setCreatedAt(msg.getCreatedAt());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{channelId}/history")
    @PreAuthorize("hasRole('CONTROL_ROOM')")
    public ResponseEntity<List<MessageHistoryResponseDTO>> getHistory(
            @PathVariable Long channelId
    ) {
        return ResponseEntity.ok(
                messageService.getChannelHistory(channelId)
        );
    }



    @PostMapping( value="/{channelId}/message/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendMessageWithUpload(
            @PathVariable Long channelId,
            @RequestParam SenderType senderType,
            @RequestParam MessageType messageType,
            @RequestParam(required = false) String textBody,
            @Parameter(
                    description = "Media file (image / video / voice)",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam MultipartFile file
    ) {

        String mediaPath = fileStorageService.storeFile(file);

        messageService.sendMessage(
                channelId,
                senderType,
                messageType,
                textBody,
                mediaPath
        );

        return ResponseEntity.ok("Message with media sent successfully");
    }
}

