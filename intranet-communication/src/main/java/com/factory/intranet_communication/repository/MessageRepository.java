package com.factory.intranet_communication.repository;

import com.factory.intranet_communication.model.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long>{

    List<MessageEntity> findByChannelIdOrderByCreatedAtDesc(Long channelId);
}
