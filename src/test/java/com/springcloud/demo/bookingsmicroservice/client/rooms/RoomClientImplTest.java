package com.springcloud.demo.bookingsmicroservice.client.rooms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClient;
import com.springcloud.demo.bookingsmicroservice.client.rooms.RoomClientImpl;
import com.springcloud.demo.bookingsmicroservice.client.rooms.dto.RoomDTO;
import com.springcloud.demo.bookingsmicroservice.exceptions.ForbiddenException;
import com.springcloud.demo.bookingsmicroservice.exceptions.InheritedException;
import feign.FeignException;
import feign.Request;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.BDDMockito.*;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class RoomClientImplTest {

    @Mock
    private RoomClient roomClient;

    @InjectMocks
    private RoomClientImpl roomClientImpl;

    @Nested
    class FindUserById {
        @Test
        void findUserById() {
            RoomDTO roomDTO = RoomDTO.builder()
                    .id(UUID.randomUUID())
                    .build();

            given(roomClient.findById(anyString())).willReturn(roomDTO);

            RoomDTO result = roomClientImpl.findById(roomDTO.getId().toString());

            assertThat(result).isEqualTo(roomDTO);
        }
    }

    @Nested
    class Fallback {
        @Test
        void whenCannotConnectToUsersService() {

            ForbiddenException response = Assertions.assertThrows(ForbiddenException.class, () -> {
                roomClientImpl.findRoomByIdFallback(UUID.randomUUID().toString(), new RuntimeException());
            });

            assertThat(response.getMessage()).isEqualTo("Rooms service not available. Try later");
        }

        @Test
        void whenReceiveClientExceptionFromUsersService() throws JsonProcessingException {
            Map body = Map.of("message", "Not found room with id");
            String bodyString = new ObjectMapper().writeValueAsString(body);

            InheritedException response = Assertions.assertThrows(InheritedException.class, () -> {
                roomClientImpl.findRoomByIdFallback(
                        UUID.randomUUID().toString(),
                        new FeignException.FeignClientException(400, null, mock(Request.class), bodyString.getBytes(), null)
                );
            });

            assertThat(response.getMessage()).isEqualTo(body.get("message"));
        }
    }
}