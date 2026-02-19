package com.factory.intranet_communication.service;
import com.factory.intranet_communication.model.*;
import com.factory.intranet_communication.repository.ChannelRepository;
import com.factory.intranet_communication.dto.MessageHistoryResponseDTO;
import com.factory.intranet_communication.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class MessageService {
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;

    public MessageService(ChannelRepository channelRepository,
                          MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public MessageEntity sendMessage(
            Long channelId,
            SenderType senderType,
            MessageType messageType,
            String textBody,
            String mediaPath
    ) {

        ChannelEntity channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));

        if (channel.getLatestMessage() != null) {
            Integer state = channel.getStateFlag();

            if (senderType == SenderType.TERMINAL && state == 1) {
                throw new IllegalStateException(
                        "Terminal must wait for Controller to read and respond");
            }

            if (senderType == SenderType.CONTROLLER && state == 0) {
                throw new IllegalStateException(
                        "Controller must wait for Terminal to read and respond");
            }
        }

        MessageEntity message = new MessageEntity();
        message.setChannel(channel);
        message.setSenderType(senderType);
        message.setContentType(messageType);
        message.setTextBody(textBody);
        message.setMediaPath(mediaPath);
        message.setCreatedAt(LocalDateTime.now());

        MessageEntity savedMessage = messageRepository.save(message);

        channel.setLatestMessage(savedMessage);
        channel.setStateFlag(senderType == SenderType.TERMINAL ? 1 : 0);
        channel.setUpdatedAt(LocalDateTime.now());

        channelRepository.save(channel);

        return savedMessage;
    }

    public ChannelEntity fetchLatest(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalStateException("Channel not found"));
    }

    public List<MessageHistoryResponseDTO> getChannelHistory(Long channelId) {

        return messageRepository
                .findByChannelIdOrderByCreatedAtDesc(channelId)
                .stream()
                .map(msg -> {
                    MessageHistoryResponseDTO dto = new MessageHistoryResponseDTO();
                    dto.setSenderType(msg.getSenderType());
                    dto.setMessageType(msg.getContentType());
                    dto.setTextBody(msg.getTextBody());
                    dto.setMediaPath(msg.getMediaPath());
                    dto.setCreatedAt(msg.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
