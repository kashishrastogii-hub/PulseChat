package com.fullstack.chatapp.repositories;

import com.fullstack.chatapp.models.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @EntityGraph(attributePaths = {"participants", "participants.user"})
    @Query("""
            select distinct c from Conversation c
            join c.participants p
            where p.user.id = :userId
            order by c.lastMessageAt desc
            """)
    List<Conversation> findConversationsForUser(@Param("userId") Long userId);

    @EntityGraph(attributePaths = {"participants", "participants.user"})
    @Query("""
            select distinct c from Conversation c
            where size(c.participants) = 2
              and exists (select p1 from c.participants p1 where p1.user.id = :firstUserId)
              and exists (select p2 from c.participants p2 where p2.user.id = :secondUserId)
            """)
    Optional<Conversation> findDirectConversation(@Param("firstUserId") Long firstUserId,
                                                  @Param("secondUserId") Long secondUserId);

    @EntityGraph(attributePaths = {"participants", "participants.user"})
    @Query("""
            select distinct c from Conversation c
            join c.participants p
            where c.id = :conversationId and p.user.id = :userId
            """)
    Optional<Conversation> findByIdAndParticipantId(@Param("conversationId") Long conversationId,
                                                    @Param("userId") Long userId);
}
