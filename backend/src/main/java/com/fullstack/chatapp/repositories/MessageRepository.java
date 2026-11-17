package com.fullstack.chatapp.repositories;

import com.fullstack.chatapp.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    Optional<Message> findTopByConversationIdOrderBySentAtDesc(Long conversationId);

    @Query("""
            select count(m) from Message m
            where m.conversation.id = :conversationId
              and m.sender.id <> :userId
              and m.readAt is null
            """)
    long countUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Modifying
    @Query("""
            update Message m
            set m.readAt = :now, m.status = com.fullstack.chatapp.models.MessageStatus.READ
            where m.conversation.id = :conversationId
              and m.sender.id <> :readerId
              and m.readAt is null
            """)
    int markConversationAsRead(@Param("conversationId") Long conversationId,
                               @Param("readerId") Long readerId,
                               @Param("now") LocalDateTime now);
}
