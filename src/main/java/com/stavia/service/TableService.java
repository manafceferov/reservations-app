package com.stavia.service;

import com.stavia.dto.table.TableCreateDto;
import com.stavia.dto.table.TableReservationCreateDto;
import com.stavia.dto.table.TableReservationResponseDto;
import com.stavia.dto.table.TableResponseDto;
import com.stavia.entity.RestaurantTable;
import com.stavia.entity.TableReservation;
import com.stavia.entity.MenuItem;
import com.stavia.entity.User;
import com.stavia.enums.ReservationStatus;
import com.stavia.exception.NotAvailableException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.TableMapper;
import com.stavia.repository.RestaurantTableRepository;
import com.stavia.repository.TableReservationRepository;
import com.stavia.repository.MenuItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final TableReservationRepository reservationRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserService userService;
    private final TableMapper tableMapper;

    public TableService(RestaurantTableRepository tableRepository,
                        TableReservationRepository reservationRepository,
                        MenuItemRepository menuItemRepository,
                        UserService userService,
                        TableMapper tableMapper
    ) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.menuItemRepository = menuItemRepository;
        this.userService = userService;
        this.tableMapper = tableMapper;
    }

    @Transactional
    public TableResponseDto create(TableCreateDto dto) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(dto.getTableNumber());
        table.setCapacity(dto.getCapacity());
        table.setLocation(dto.getLocation());
        return tableMapper.toResponseDto(tableRepository.save(table));
    }

    @Transactional
    public List<TableResponseDto> createBatch(List<TableCreateDto> dtos) {
        List<RestaurantTable> tables = dtos.stream().map(dto -> {
            RestaurantTable table = new RestaurantTable();
            table.setTableNumber(dto.getTableNumber());
            table.setCapacity(dto.getCapacity());
            table.setLocation(dto.getLocation());
            return table;
        }).toList();
        return tableRepository.saveAll(tables)
                .stream().map(tableMapper::toResponseDto).toList();
    }

    public List<TableResponseDto> getAll() {
        return tableRepository.findAllByDeletedFalse()
                .stream().map(tableMapper::toResponseDto).toList();
    }

    public List<TableResponseDto> getAvailable(String date, String time) {
        LocalDate localDate = LocalDate.parse(date);
        LocalTime localTime = LocalTime.parse(time);
        return tableRepository.findAvailableTables(localDate, localTime)
                .stream().map(tableMapper::toResponseDto).toList();
    }

    @Transactional
    public TableReservationResponseDto reserve(TableReservationCreateDto dto, Long userId) {
        User user = userService.findById(userId);
        RestaurantTable table = tableRepository.findById(dto.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Masa tapılmadı"));

        LocalDate date = LocalDate.parse(dto.getReservationDate());
        LocalTime time = LocalTime.parse(dto.getReservationTime());

        List<RestaurantTable> available = tableRepository.findAvailableTables(date, time);
        if (available.stream().noneMatch(t -> t.getId().equals(table.getId())))
            throw new NotAvailableException("Bu masa seçilmiş vaxt üçün mövcud deyil");

        TableReservation reservation = new TableReservation();
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setReservationDate(date);
        reservation.setReservationTime(time);
        reservation.setGuestCount(dto.getGuestCount());
        reservation.setNotes(dto.getNotes());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        if (dto.getMenuItemIds() != null && !dto.getMenuItemIds().isEmpty()) {
            List<MenuItem> items = dto.getMenuItemIds().stream()
                    .map(id -> menuItemRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
            reservation.setMenuItems(items);
        }
        return tableMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }

    public Page<TableReservationResponseDto> getUserReservations(Long userId, Pageable pageable) {
        return reservationRepository.findAllByUserIdAndDeletedFalse(userId, pageable)
                .map(tableMapper::toReservationResponseDto);
    }

    public Page<TableReservationResponseDto> getAllReservations(Pageable pageable) {
        return reservationRepository.findAllByDeletedFalse(pageable)
                .map(tableMapper::toReservationResponseDto);
    }

    @Transactional
    public TableReservationResponseDto cancelReservation(Long reservationId, Long userId) {
        TableReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasiya tapılmadı"));

        if (!reservation.getUser().getId().equals(userId))
            throw new ResourceNotFoundException("Bu rezervasiya sizə aid deyil");
        reservation.setStatus(ReservationStatus.CANCELLED);
        return tableMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }
}