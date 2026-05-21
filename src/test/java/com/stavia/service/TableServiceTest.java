package com.stavia.service;

import com.stavia.dto.table.*;
import com.stavia.entity.MenuItem;
import com.stavia.entity.RestaurantTable;
import com.stavia.entity.TableReservation;
import com.stavia.entity.User;
import com.stavia.enums.ReservationStatus;
import com.stavia.exception.NotAvailableException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.TableMapper;
import com.stavia.repository.MenuItemRepository;
import com.stavia.repository.RestaurantTableRepository;
import com.stavia.repository.TableReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private TableReservationRepository reservationRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserService userService;

    @Mock
    private TableMapper tableMapper;

    @InjectMocks
    private TableService service;

    @Test
    void createSuccess() {

        TableCreateDto dto = new TableCreateDto();
        dto.setTableNumber(1);
        dto.setCapacity(4);
        dto.setLocation("Window");

        RestaurantTable table = new RestaurantTable();

        when(tableRepository.save(any()))
                .thenReturn(table);

        when(tableMapper.toResponseDto(any()))
                .thenReturn(new TableResponseDto());

        TableResponseDto response = service.create(dto);

        assertNotNull(response);

        verify(tableRepository).save(any(RestaurantTable.class));
    }

    @Test
    void createBatchSuccess() {

        TableCreateDto dto = new TableCreateDto();
        dto.setTableNumber(1);
        dto.setCapacity(4);

        when(tableRepository.saveAll(any()))
                .thenReturn(List.of(new RestaurantTable()));

        when(tableMapper.toResponseDto(any()))
                .thenReturn(new TableResponseDto());

        List<TableResponseDto> result =
                service.createBatch(List.of(dto));

        assertEquals(1, result.size());
    }

    @Test
    void getAllSuccess() {

        when(tableRepository.findAllByDeletedFalse())
                .thenReturn(List.of(new RestaurantTable()));

        when(tableMapper.toResponseDto(any()))
                .thenReturn(new TableResponseDto());

        List<TableResponseDto> result = service.getAll();

        assertEquals(1, result.size());
    }

    @Test
    void getAvailableSuccess() {

        when(tableRepository.findAvailableTables(
                any(LocalDate.class),
                any(LocalTime.class)
        )).thenReturn(List.of(new RestaurantTable()));

        when(tableMapper.toResponseDto(any()))
                .thenReturn(new TableResponseDto());

        List<TableResponseDto> result =
                service.getAvailable("2026-05-01", "18:00");

        assertEquals(1, result.size());
    }

    @Test
    void reserveSuccessWithoutMenuItems() {

        User user = new User();

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);

        TableReservationCreateDto dto =
                new TableReservationCreateDto();

        dto.setTableId(1L);
        dto.setReservationDate("2026-05-01");
        dto.setReservationTime("19:00");
        dto.setGuestCount(2);

        when(userService.findById(1L))
                .thenReturn(user);

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(table));

        when(tableRepository.findAvailableTables(any(), any()))
                .thenReturn(List.of(table));

        when(reservationRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(tableMapper.toReservationResponseDto(any()))
                .thenReturn(new TableReservationResponseDto());

        TableReservationResponseDto response =
                service.reserve(dto, 1L);

        assertNotNull(response);

        verify(reservationRepository)
                .save(any(TableReservation.class));
    }

    @Test
    void reserveSuccessWithMenuItems() {

        User user = new User();

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);

        MenuItem menuItem = new MenuItem();
        menuItem.setId(10L);

        TableReservationCreateDto dto =
                new TableReservationCreateDto();

        dto.setTableId(1L);
        dto.setReservationDate("2026-05-01");
        dto.setReservationTime("19:00");
        dto.setGuestCount(2);
        dto.setMenuItemIds(List.of(10L));

        when(userService.findById(1L))
                .thenReturn(user);

        when(tableRepository.findById(1L))
                .thenReturn(Optional.of(table));

        when(tableRepository.findAvailableTables(any(), any()))
                .thenReturn(List.of(table));

        when(menuItemRepository.findById(10L))
                .thenReturn(Optional.of(menuItem));

        when(reservationRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        when(tableMapper.toReservationResponseDto(any()))
                .thenReturn(new TableReservationResponseDto());

        TableReservationResponseDto response =
                service.reserve(dto, 1L);

        assertNotNull(response);
    }

    @Test
    void reserveShouldThrowWhenTableNotFound() {

        TableReservationCreateDto dto =
                new TableReservationCreateDto();

        dto.setTableId(1L);

        when(userService.findById(any()))
                .thenReturn(new User());

        when(tableRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.reserve(dto, 1L));
    }

    @Test
    void reserveShouldThrowWhenTableUnavailable() {

        User user = new User();

        RestaurantTable table = new RestaurantTable();
        table.setId(99L);

        TableReservationCreateDto dto =
                new TableReservationCreateDto();

        dto.setTableId(1L);
        dto.setReservationDate("2026-05-01");
        dto.setReservationTime("18:00");

        when(userService.findById(any()))
                .thenReturn(user);

        when(tableRepository.findById(any()))
                .thenReturn(Optional.of(table));

        when(tableRepository.findAvailableTables(any(), any()))
                .thenReturn(List.of());

        assertThrows(NotAvailableException.class,
                () -> service.reserve(dto, 1L));
    }

    @Test
    void getUserReservationsSuccess() {

        Pageable pageable = PageRequest.of(0, 10);

        when(reservationRepository
                .findAllByUserIdAndDeletedFalse(1L, pageable))
                .thenReturn(new PageImpl<>(List.of(
                        new TableReservation()
                )));

        when(tableMapper.toReservationResponseDto(any()))
                .thenReturn(new TableReservationResponseDto());

        Page<TableReservationResponseDto> result =
                service.getUserReservations(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getAllReservationsSuccess() {

        Pageable pageable = PageRequest.of(0, 10);

        when(reservationRepository
                .findAllByDeletedFalse(pageable))
                .thenReturn(new PageImpl<>(List.of(
                        new TableReservation()
                )));

        when(tableMapper.toReservationResponseDto(any()))
                .thenReturn(new TableReservationResponseDto());

        Page<TableReservationResponseDto> result =
                service.getAllReservations(pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void cancelReservationSuccess() {

        User user = new User();
        user.setId(1L);

        TableReservation reservation =
                new TableReservation();

        reservation.setUser(user);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.save(any()))
                .thenReturn(reservation);

        when(tableMapper.toReservationResponseDto(any()))
                .thenReturn(new TableReservationResponseDto());

        TableReservationResponseDto response =
                service.cancelReservation(1L, 1L);

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );

        assertNotNull(response);
    }

    @Test
    void cancelReservationShouldThrowWhenReservationNotFound() {

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.cancelReservation(1L, 1L));
    }

    @Test
    void cancelReservationShouldThrowWhenUserNotOwner() {

        User user = new User();
        user.setId(99L);

        TableReservation reservation =
                new TableReservation();

        reservation.setUser(user);

        when(reservationRepository.findById(1L))
                .thenReturn(Optional.of(reservation));

        assertThrows(ResourceNotFoundException.class,
                () -> service.cancelReservation(1L, 1L));
    }
}