package com.stavia.service;

import com.stavia.dto.room.*;
import com.stavia.entity.*;
import com.stavia.enums.ReservationStatus;
import com.stavia.enums.RoomType;
import com.stavia.exception.NotAvailableException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.RoomMapper;
import com.stavia.repository.RoomRepository;
import com.stavia.repository.RoomReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomReservationRepository reservationRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService service;

    @Test
    void createSuccess() {

        RoomCreateDto dto = new RoomCreateDto();
        dto.setRoomNumber("101");
        dto.setRoomType(RoomType.STANDARD.name());

        when(roomRepository.save(any()))
                .thenReturn(new Room());

        when(roomMapper.toResponseDto(any()))
                .thenReturn(new RoomResponseDto());

        RoomResponseDto response = service.create(dto);

        assertNotNull(response);
    }

    @Test
    void getAvailableSuccess() {
        when(roomRepository.findAvailableRooms(any(), any())).thenReturn(List.of(new Room()));
        when(roomMapper.toResponseDto(any())).thenReturn(new RoomResponseDto());

        List<RoomResponseDto> response = service.getAvailable("2026-05-01", "2026-05-05");

        assertNotNull(response);
        assertFalse(response.isEmpty());
        verify(roomRepository).findAvailableRooms(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-05-05"));
    }

    @Test
    void getUserReservationsSuccess() {
        when(reservationRepository.findAllByUserIdAndDeletedFalse(eq(1L), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(roomMapper.toReservationResponseDto(any())).thenReturn(new RoomReservationResponseDto());

        Page<RoomReservationResponseDto> response = service.getUserReservations(1L, Pageable.unpaged());

        assertNotNull(response);
        verify(reservationRepository).findAllByUserIdAndDeletedFalse(eq(1L), any(Pageable.class));
    }

    @Test
    void getAllReservationsSuccess() {
        when(reservationRepository.findAllByDeletedFalse(any(Pageable.class)))
                .thenReturn(Page.empty());
        when(roomMapper.toReservationResponseDto(any())).thenReturn(new RoomReservationResponseDto());

        Page<RoomReservationResponseDto> response = service.getAllReservations(Pageable.unpaged());

        assertNotNull(response);
        verify(reservationRepository).findAllByDeletedFalse(any(Pageable.class));
    }

    @Test
    void reserveSuccess() {

        User user = new User();

        Room room = new Room();
        room.setId(1L);
        room.setPricePerNight(100.0);

        RoomReservationCreateDto dto = new RoomReservationCreateDto();
        dto.setRoomId(1L);
        dto.setCheckIn("2026-05-01");
        dto.setCheckOut("2026-05-03");
        dto.setGuestCount(2);

        when(userService.findById(1L)).thenReturn(user);

        when(roomRepository.findById(1L))
                .thenReturn(Optional.of(room));

        when(roomRepository.findAvailableRooms(any(), any()))
                .thenReturn(List.of(room));

        when(reservationRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(roomMapper.toReservationResponseDto(any()))
                .thenReturn(new RoomReservationResponseDto());

        RoomReservationResponseDto response =
                service.reserve(dto, 1L);

        assertNotNull(response);
    }

    @Test
    void reserveShouldThrowWhenCheckoutInvalid() {

        RoomReservationCreateDto dto =
                new RoomReservationCreateDto();

        dto.setRoomId(1L);
        dto.setCheckIn("2026-05-03");
        dto.setCheckOut("2026-05-01");

        when(userService.findById(any()))
                .thenReturn(new User());

        when(roomRepository.findById(any()))
                .thenReturn(Optional.of(new Room()));

        assertThrows(NotAvailableException.class,
                () -> service.reserve(dto, 1L));
    }

    @Test
    void reserveShouldThrowWhenRoomUnavailable() {

        Room room = new Room();
        room.setId(99L);

        RoomReservationCreateDto dto =
                new RoomReservationCreateDto();

        dto.setRoomId(1L);
        dto.setCheckIn("2026-05-01");
        dto.setCheckOut("2026-05-05");

        when(userService.findById(any()))
                .thenReturn(new User());

        when(roomRepository.findById(any()))
                .thenReturn(Optional.of(room));

        when(roomRepository.findAvailableRooms(any(), any()))
                .thenReturn(List.of());

        assertThrows(NotAvailableException.class,
                () -> service.reserve(dto, 1L));
    }

    @Test
    void checkInSuccess() {

        RoomReservation reservation =
                new RoomReservation();

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        when(roomMapper.toReservationResponseDto(any()))
                .thenReturn(new RoomReservationResponseDto());

        RoomReservationResponseDto response =
                service.checkIn(1L);

        assertEquals(ReservationStatus.CHECKED_IN,
                reservation.getStatus());

        assertNotNull(response);
    }

    @Test
    void createBatchSuccess() {
        RoomCreateDto dto = new RoomCreateDto();
        dto.setRoomNumber("102");
        dto.setRoomType("STANDARD");

        when(roomRepository.saveAll(any())).thenReturn(List.of(new Room()));
        when(roomMapper.toResponseDto(any())).thenReturn(new RoomResponseDto());

        List<RoomResponseDto> response = service.createBatch(List.of(dto));
        assertFalse(response.isEmpty());
    }

    @Test
    void getAllSuccess() {
        when(roomRepository.findAllByDeletedFalse(any())).thenReturn(Page.empty());
        var response = service.getAll(Pageable.unpaged());
        assertNotNull(response);
    }

    @Test
    void editSuccess() {
        Room room = new Room();
        RoomEditDto dto = new RoomEditDto();
        dto.setCapacity(5);

        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomRepository.save(any())).thenReturn(room);
        when(roomMapper.toResponseDto(any())).thenReturn(new RoomResponseDto());

        service.edit(1L, dto);
        assertEquals(5, room.getCapacity());
    }

    @Test
    void deleteSuccess() {
        Room room = new Room();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        service.delete(1L);
        assertTrue(room.getDeleted());
        verify(roomRepository).save(room);
    }

    @Test
    void cancelReservationSuccess() {
        User user = new User();
        user.setId(1L);
        RoomReservation res = new RoomReservation();
        res.setUser(user);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any())).thenReturn(res);
        when(roomMapper.toReservationResponseDto(any())).thenReturn(new RoomReservationResponseDto());

        service.cancelReservation(1L, 1L);
        assertEquals(ReservationStatus.CANCELLED, res.getStatus());
    }

    @Test
    void checkOutSuccess() {
        Room room = new Room();
        RoomReservation res = new RoomReservation();
        res.setRoom(room);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(res));
        when(reservationRepository.save(any())).thenReturn(res);
        when(roomMapper.toReservationResponseDto(any())).thenReturn(new RoomReservationResponseDto());

        service.checkOut(1L);
        assertEquals(ReservationStatus.CHECKED_OUT, res.getStatus());
        assertTrue(room.getAvailable());
    }

    @Test
    void findByIdThrowsException() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }
}