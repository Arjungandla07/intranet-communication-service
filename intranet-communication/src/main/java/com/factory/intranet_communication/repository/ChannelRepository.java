package com.factory.intranet_communication.repository;

import com.factory.intranet_communication.model.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, Long>{

}
