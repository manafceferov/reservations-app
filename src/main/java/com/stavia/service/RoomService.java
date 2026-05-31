package com.stavia.service;

import com.stavia.dto.room.RoomCreateDto;
import com.stavia.dto.room.RoomEditDto;
import com.stavia.dto.room.RoomReservationCreateDto;
import com.stavia.dto.room.RoomReservationResponseDto;
import com.stavia.dto.room.RoomResponseDto;
import com.stavia.entity.Room;
import com.stavia.entity.RoomReservation;
import com.stavia.entity.User;
import com.stavia.enums.ReservationStatus;
import com.stavia.enums.RoomType;
import com.stavia.exception.NotAvailableException;
import com.stavia.exception.ResourceNotFoundException;
import com.stavia.mapper.RoomMapper;
import com.stavia.repository.RoomRepository;
import com.stavia.repository.RoomReservationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomReservationRepository reservationRepository;
    private final UserService userService;
    private final RoomMapper roomMapper;

    public RoomService(RoomRepository roomRepository,
                       RoomReservationRepository reservationRepository,
                       UserService userService,
                       RoomMapper roomMapper
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.userService = userService;
        this.roomMapper = roomMapper;
    }

    @Transactional
    public RoomResponseDto create(RoomCreateDto dto) {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setRoomType(RoomType.valueOf(dto.getRoomType()));
        room.setPricePerNight(dto.getPricePerNight());
        room.setCapacity(dto.getCapacity());
        room.setDescription(dto.getDescription());
        return roomMapper.toResponseDto(roomRepository.save(room));
    }

    @Transactional
    public List<RoomResponseDto> createBatch(List<RoomCreateDto> dtos) {
        List<Room> rooms = dtos.stream().map(dto -> {
            Room room = new Room();
            room.setRoomNumber(dto.getRoomNumber());
            room.setRoomType(RoomType.valueOf(dto.getRoomType()));
            room.setPricePerNight(dto.getPricePerNight());
            room.setCapacity(dto.getCapacity());
            room.setDescription(dto.getDescription());
            return room;
        }).toList();
        return roomRepository.saveAll(rooms)
                .stream().map(roomMapper::toResponseDto).toList();
    }

    public Page<RoomResponseDto> getAll(Pageable pageable) {
        return roomRepository.findAllByDeletedFalse(pageable)
                .map(roomMapper::toResponseDto);
    }

    public List<RoomResponseDto> getAvailable(String checkIn, String checkOut) {
        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);
        return roomRepository.findAvailableRooms(in, out)
                .stream().map(roomMapper::toResponseDto).toList();
    }

    @Transactional
    public RoomResponseDto edit(Long id, RoomEditDto dto) {
        Room room = findById(id);
        if (dto.getPricePerNight() != null) room.setPricePerNight(dto.getPricePerNight());
        if (dto.getCapacity() != null) room.setCapacity(dto.getCapacity());
        if (dto.getDescription() != null) room.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) room.setAvailable(dto.getAvailable());
        return roomMapper.toResponseDto(roomRepository.save(room));
    }

    @Transactional
    public void delete(Long id) {
        Room room = findById(id);
        room.setDeleted(true);
        roomRepository.save(room);
    }

    private void validateDates(LocalDate checkIn,
                               LocalDate checkOut
    ) {
        if (!checkOut.isAfter(checkIn))
            throw new NotAvailableException("Çıxış tarixi giriş tarixindən sonra olmalıdır");
    }

    private void validateRoomAvailability(Room room,
                                          LocalDate checkIn,
                                          LocalDate checkOut
    ) {
        boolean isAvailable = roomRepository.findAvailableRooms(checkIn, checkOut)
                .stream()
                .anyMatch(r -> r.getId().equals(room.getId()));
        if (!isAvailable)
            throw new NotAvailableException("Bu otaq seçilmiş tarixlər üçün mövcud deyil");
    }

    private RoomReservation buildReservation(User user,
                                             Room room,
                                             RoomReservationCreateDto dto,
                                             LocalDate checkIn,
                                             LocalDate checkOut
    ) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        RoomReservation reservation = new RoomReservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setGuestCount(dto.getGuestCount());
        reservation.setTotalPrice(nights * room.getPricePerNight());
        reservation.setNotes(dto.getNotes());
        reservation.setStatus(ReservationStatus.CONFIRMED);
        return reservation;
    }

    @Transactional
    public RoomReservationResponseDto reserve(RoomReservationCreateDto dto,
                                              Long userId
    ) {
        User user = userService.findById(userId);
        Room room = findById(dto.getRoomId());
        LocalDate checkIn = LocalDate.parse(dto.getCheckIn());
        LocalDate checkOut = LocalDate.parse(dto.getCheckOut());
        validateDates(checkIn, checkOut);
        validateRoomAvailability(room, checkIn, checkOut);
        RoomReservation reservation = buildReservation(user, room, dto, checkIn, checkOut);
        return roomMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }

    public Page<RoomReservationResponseDto> getUserReservations(Long userId,
                                                                Pageable pageable
    ) {
        return reservationRepository.findAllByUserIdAndDeletedFalse(userId, pageable)
                .map(roomMapper::toReservationResponseDto);
    }

    public Page<RoomReservationResponseDto> getAllReservations(Pageable pageable) {
        return reservationRepository.findAllByDeletedFalse(pageable)
                .map(roomMapper::toReservationResponseDto);
    }

    @Transactional
    public RoomReservationResponseDto cancelReservation(Long reservationId,
                                                        Long userId
    ) {
        RoomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasiya tapılmadı"));

        if (!reservation.getUser().getId().equals(userId))
            throw new ResourceNotFoundException("Bu rezervasiya sizə aid deyil");

        reservation.setStatus(ReservationStatus.CANCELLED);
        return roomMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }

    @Transactional
    public RoomReservationResponseDto checkIn(Long reservationId) {
        RoomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasiya tapılmadı"));
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        return roomMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }

    @Transactional
    public RoomReservationResponseDto checkOut(Long reservationId) {
        RoomReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasiya tapılmadı"));
        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        reservation.getRoom().setAvailable(true);
        return roomMapper.toReservationResponseDto(reservationRepository.save(reservation));
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Otaq tapılmadı"));
    }
}