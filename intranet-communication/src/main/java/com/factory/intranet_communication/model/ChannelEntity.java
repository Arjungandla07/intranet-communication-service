package com.factory.intranet_communication.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
public class ChannelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long id;

    @Column(name = "state_flag", nullable = false)
    private Integer stateFlag;

    @OneToOne
    @JoinColumn(name = "latest_message_id")
    private MessageEntity latestMessage;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStateFlag() { return stateFlag; }
    public void setStateFlag(Integer stateFlag) { this.stateFlag = stateFlag; }
    public MessageEntity getLatestMessage() { return latestMessage; }
    public void setLatestMessage(MessageEntity latestMessage) { this.latestMessage = latestMessage; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
