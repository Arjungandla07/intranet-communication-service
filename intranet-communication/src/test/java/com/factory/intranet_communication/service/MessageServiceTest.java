package com.factory.intranet_communication.service;

import com.factory.intranet_communication.dto.MessageHistoryResponseDTO;
import com.factory.intranet_communication.model.*;
import com.factory.intranet_communication.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    ChannelRepository channelRepository;

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    MessageService messageService;

    private ChannelEntity mockChannel(Integer stateFlag) {
        ChannelEntity channel = new ChannelEntity();
        channel.setId(1L);
        channel.setStateFlag(stateFlag);
        channel.setUpdatedAt(LocalDateTime.now());
        return channel;
    }

    @Test
    void shouldSendMessageFromTerminalSuccessfully() {

        ChannelEntity channel = mockChannel(null);

        when(channelRepository.findById(1L))
                .thenReturn(Optional.of(channel));

        when(messageRepository.save(any(MessageEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        MessageEntity result = messageService.sendMessage(
                1L,
                SenderType.TERMINAL,
                MessageType.TEXT,
                "Hello Controller",
                null
        );

        assertNotNull(result);
        assertEquals(SenderType.TERMINAL, result.getSenderType());
        assertEquals("Hello Controller", result.getTextBody());

        verify(channelRepository).save(channel);
        verify(messageRepository).save(any(MessageEntity.class));
    }

    @Test
    void terminalShouldNotSendTwice() {

        ChannelEntity channel = mockChannel(1); // terminal already sent
        channel.setLatestMessage(new MessageEntity());

        when(channelRepository.findById(1L))
                .thenReturn(Optional.of(channel));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> messageService.sendMessage(
                        1L,
                        SenderType.TERMINAL,
                        MessageType.TEXT,
                        "Second message",
                        null
                )
        );

        assertEquals(
                "Terminal must wait for Controller to read and respond",
                ex.getMessage()
        );
    }

    @Test
    void controllerShouldNotSendOutOfTurn() {

        ChannelEntity channel = mockChannel(0); // controller already sent
        channel.setLatestMessage(new MessageEntity());

        when(channelRepository.findById(1L))
                .thenReturn(Optional.of(channel));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> messageService.sendMessage(
                        1L,
                        SenderType.CONTROLLER,
                        MessageType.TEXT,
                        "Controller reply",
                        null
                )
        );

        assertEquals(
                "Controller must wait for Terminal to read and respond",
                ex.getMessage()
        );
    }

    @Test
    void shouldThrowWhenChannelNotFound() {

        when(channelRepository.findById(99L))
                .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> messageService.sendMessage(
                        99L,
                        SenderType.TERMINAL,
                        MessageType.TEXT,
                        "Hello",
                        null
                )
        );

        assertEquals("Channel not found", ex.getMessage());
    }


    @Test
    void shouldReturnChannelHistory() {

        MessageEntity msg = new MessageEntity();
        msg.setSenderType(SenderType.TERMINAL);
        msg.setContentType(MessageType.TEXT);
        msg.setTextBody("History msg");
        msg.setCreatedAt(LocalDateTime.now());

        when(messageRepository.findByChannelIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(msg));

        List<MessageHistoryResponseDTO> history =
                messageService.getChannelHistory(1L);

        assertEquals(1, history.size());
        assertEquals("History msg", history.get(0).getTextBody());
    }



}
