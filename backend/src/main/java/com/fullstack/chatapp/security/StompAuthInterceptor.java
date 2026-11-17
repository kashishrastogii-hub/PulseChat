package com.fullstack.chatapp.security;

import com.fullstack.chatapp.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * Authenticates the STOMP CONNECT frame with the same JWT used by the REST API, so that
 * message mappings receive a real Principal and no anonymous client can publish messages.
 */
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            accessor = StompHeaderAccessor.wrap(message);
        }
        boolean isConnect = StompCommand.CONNECT.equals(accessor.getCommand())
                || SimpMessageType.CONNECT.equals(accessor.getMessageType());
        if (!isConnect) {
            return message;
        }

        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token) || !jwtUtils.validateJwtToken(token)) {
            throw new MessageDeliveryException("A valid JWT is required to open a WebSocket session");
        }

        String username = jwtUtils.getUsernameFromJwtToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        accessor.setUser(authentication);

        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String bearer = firstValue(accessor.getNativeHeader("Authorization"));
        if (StringUtils.hasText(bearer)) {
            return bearer.startsWith("Bearer ") ? bearer.substring(7) : bearer;
        }
        return firstValue(accessor.getNativeHeader("token"));
    }

    private String firstValue(Collection<String> values) {
        return values == null || values.isEmpty() ? null : values.iterator().next();
    }
}
